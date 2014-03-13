'use strict';

define(function(require){
    var _ = require('underscore'),
        log = require('log'),
        i18n = require('locale/i18n'),
        ItemView = require('view/item-view'),
        ViewHelpers = require('view/view-helpers'),
        viewTemplate = require('text!template/sync/sync-source.html');

    /**
     * Ammount of seconds between status refreshes
     * @type Number
     */
    var NEXT_REFRESH_LONG = 60,
        NEXT_REFRESH_SHORT = 5,
        /**
         * Timeout to wait to ensure that server executes task
         * @type Number
         */
        NEXT_REFRESH_DELAY = 1000;

    /**
     * Helpers for dealing with SyncSource model.
     * All methods are executed within the context of model instance.
     * @class
     */
    var templateHelpers = {
        /**
         * Returns status message
         * @see http://confluence.hflabs.ru/display/RECODER/UI.Sync.Main#UI.Sync.Main-Статусзадачи
         */
        getStatusDescription: function(){
            var t = ['model.sync.state', this.status],
                model = this.getModel();
            if (model.isStatusReady() && this.result && this.result.status) {
                t.push(this.result.status);
                if (model.isResultIncomplete()) {
                    t.push('ERROR');
                }
            }
            return i18n.tc(t.join('.'));
        },
        /**
         * Calculates the class for icon
         * @returns {string}    'ready'|'error'|'running'|'cancel'
         */
        getIconClass: function(){
            if (this.getModel().isExecuting()) {
                return 'running';
            }
            if (this.getModel().hasErrors()) {
                return 'error';
            }
            if (this.getModel().isResultCanceled()) {
                return 'cancel';
            }
            return 'ready';
        },
        /**
         * Returns datetime message.
         *
         * Uses <code>endDate</code> for completed tasks and nothing for running ones
         * @returns {string}
         */
        adequateDate: function(){
            if (this.result) {
                // for running tasks show no date
                var date = this.getModel().isExecuting() ? null : this.result.endDate;
            }
            return date ? i18n.t('model.time.in', 1, [date.toDateString(), date.toTimeString()]) : '';
        },
        /**
         * Returns human-readable dictionary count for specific sync execution.
         * @returns {string}
         */
        dictionaryCount: function() {
            var totalCount = this.result.content.totalCount;
            var errorCount = this.result.content.errorCount;
            if (errorCount > 0) {
                return i18n.t('model.dictionary.errors', errorCount, [errorCount, totalCount]);
            } else {
                return totalCount + ' ' + i18n.t('model.dictionary', totalCount);
            }
        },
        /**
         * Wraps message with error-styled span if there are errors
         * @param {boolean} hasError indicates if there are errors
         * @param {string}  message  message to show
         * @returns {string} Wrapped message
         */
        wrapError: function(hasError, message) {
            if (hasError) {
                return '<span data-action="show-errors" title="'
                        + i18n.tc('view.sync.show_last_errors') + '">'
                        + _.escape(message)
                        + '</span>';
            } else {
                return _.escape(message);
            }
        }
    };

    /**
     * Individual sync source widget.
     * @class
     */
    var SyncSourceView = ItemView.extend({
        tagName: 'tbody',
        template: _.template(viewTemplate),
        templateHelpers: function(){
            var self = this;
            return _.extend(
                {
                    getModel: function(){
                        return self.model;
                    },
                    nextSync: self.nextSync
                }, 
                ViewHelpers.templateHelpers, 
                templateHelpers
            )
        },
        events: {
            'click [data-action="show-errors"]': 'openErrorsPopup',
            'click [data-action="run-stop"]': 'runOrStop',
            'click [data-action="edit"]': 'openCronPopup',
            'click [data-action="settings"]': 'openParametersPopup',
            'click [data-action="delete"]': 'deleteModel'
        },
        modelEvents : {
            'change:descriptor': 'render',
            'change:progress': 'onProgressChanged',
            'change:status': 'render',
            'change:result': 'render'
        },
        ui: {
            percent: '[data-label="percent"]',
            progress: '[data-label="progress"]',
            step: '[data-label="step"]',
            nextDate: '[data-label="nextDate"]'
        },
        initialize: function(){
            this.refreshTimer = null;
            this.descriptorModel = this.model.get('descriptor');
        },
        onRender: function(){
            this.planDateRefresh();
        },
        onClose: function(){
            clearTimeout(this.refreshTimer);
        },
        /**
         * Set timer to refresh time left before next sync execution
         */
        planDateRefresh: function(){
            clearTimeout(this.refreshTimer);
            var next = this.refreshAfter();
            if (_.isNumber(next)) {
                if(next >= NEXT_REFRESH_SHORT) {
                    this.refreshTimer = setTimeout(_.bind(this.refreshNextDate, this), next * 1000);
                } else {
                    var self = this;
                    this.refreshTimer = setTimeout(function(){
                        self.model.trigger('expire');
                        self.model.fetch();
                    }, next * 1000 + NEXT_REFRESH_DELAY);
                }
            }
        },
        /**
         * Returns the amount of seconds, after which the view should be re-rendered.
         * @returns {number}
         */
        refreshAfter: function() {
            var next = this.descriptorModel.get('nextScheduledDate');
            if (!next) {
                return null;
            }
            next = (next.valueOf() - new Date().valueOf()) / 1000;
            next = (next > NEXT_REFRESH_LONG * 2) ? NEXT_REFRESH_LONG :
                (next > NEXT_REFRESH_LONG) ? (next - NEXT_REFRESH_LONG) :
                    (next > NEXT_REFRESH_SHORT) ? NEXT_REFRESH_SHORT :
                        (next > 0) ? next : 0;
            log.debug('Refresh "' + this.descriptorModel.get('description') + '" after ' + next + ' sec.');
            return next;
        },
        /**
         * Re-renders message with time left before next sync execution
         */
        refreshNextDate: function(){
            var nextScheduledDate = this.descriptorModel.get('nextScheduledDate');
            if (nextScheduledDate) {
                this.ui.nextDate.show()
                    .attr('title', nextScheduledDate.toString())
                    .text(this.nextSync(nextScheduledDate));
                this.planDateRefresh();
            } else {
                this.ui.nextDate.hide();
            }
        },
        /**
         * Returns human-readable amount of time after which sync will be executed.
         * @param {Date} next   LocalDate object, containing the moment of next execution
         * @returns {string}
         * {@link: http://confluence.hflabs.ru/display/RECODER/UI.Sync.Main#UI.Sync.Main-Время,оставшеесядоследующегозапуска}
         */
        nextSync: function(next) {

            function _time_in (value, units) {
                return i18n.t('model.time.in_' + units, value, [value]);
            }

            if (!next || !next.valueOf() )
                return '';

            var value = Math.round( (next.valueOf() - new Date().valueOf()) / 1000);
            if (value < 60) {
                return _time_in(Math.round(value), 'second');
            }
            value = value/60;
            if (value < 60) {
                return _time_in(Math.round(value), 'minute');
            }
            value = value/60;
            if (value < 24) {
                return _time_in(Math.round(value), 'hour');
            }
            value = value/24;
            if (value < 7) {
                return _time_in(Math.round(value), 'day');
            }
            if (value < 31) {
                return _time_in(Math.round(value/7), 'week');
            }
            if (value < 365) {
                return _time_in(Math.round(value/31), 'month');
            }
            return _time_in(1, 'infinite');
        },

        openCronPopup: function(){
            var descriptor = this.model.get('descriptor');
            this.model.trigger('edit:cron', descriptor);
        },
        openParametersPopup: function(){
            var descriptor = this.model.get('descriptor');
            this.model.trigger('edit:parameters', descriptor);
        },
        openErrorsPopup: function(){
            this.model.trigger('edit:errors', this.model);
        },

        runOrStop: function(){
            this.model.toggleExecute();
        },

        onProgressChanged: function(){
            var progress = this.model.get("progress"),
                percent = Math.max(0, progress && progress.percent || 0);
            this.ui.percent.text(percent + '%');
            this.ui.step.text(progress && progress.step || '');
            this.ui.progress.css('width', percent + '%');
        },
        deleteModel: function(){
            this.model.trigger('sync:delete', this.model);
        }
    });

    return SyncSourceView;

});
