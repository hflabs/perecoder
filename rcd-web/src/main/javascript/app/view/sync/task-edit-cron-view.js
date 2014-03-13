'use strict';

define([
    'underscore',
    'locale/i18n',
    'view/item-view',
    'view/view-helpers',
    'common/cron-util',
    'text!template/sync/task-descriptor-cron-view.html'
], function (_, i18n, ItemView, ViewHelpers, Cron, editTemplate) {

    var TaskEditCronView = ItemView.extend({
        className: 'modal fade in',
        attributes: {style: 'display:none'},
        
        template: _.template(editTemplate),
        templateHelpers: function(){
            var tempCron = this.tempCron;
            var cronEnabled = this.cronEnabled;
            return _.extend({
                    getTempCron: function() {
                        return tempCron;
                    },
                    isCronEnabled: function(){
                        return cronEnabled;
                    }
                },
                ViewHelpers.templateHelpers
            );
        },
        events: {
            'keyup input[data-cron]': 'cronChanged',
            'change select[data-cron]': 'cronChanged',
            'switch-change .make-switch': 'onSwitchChanged',
            'click [data-action="save"]': 'applyEdit'
        },
        initialize: function(){
            var cron = this.model.get('cron');
            this.cronEnabled = true;
            this.tempCron = new Cron(cron.toString() || '0 * * * * *');
        },
        ui: {
            'switch': '.make-switch',
            'alert': '.alert',
            'inputs': '[data-cron]',
            'btnSave': '[data-action="save"]',
            'details': '[data-field="cron-details"]',
            'selects': 'select.selectpicker'
        },
        onRender: function(){
            this.ui.switch.not('.has-switch').bootstrapSwitch();
            this.ui.selects.selectpicker();
            this.updateAlert();
        },
        onShow: function(){
            this.$el.modal('show');
            this.resetInputsSelection();
        },
        onClose: function(){
            this.$el.modal('hide');
        },

        resetInputsSelection: function(){
            this.ui.inputs.each(function(){
                this.value = this.value;
            });
        },
        updateAlert: function(){
            this.ui.alert
                .toggleClass('alert-error', !this.tempCron.isCorrect() )
                .text( i18n.capitalize( this.cronEnabled ? this.tempCron.describe() : i18n.t('model.cron.undefined')) );
        },
        
        cronChanged: function(){
            var cron = this.tempCron;
            cron.schedule.s.fromString('0');
            this.ui.inputs.each(function(){
                var $el = $(this),
                    cronPart = $el.attr('data-cron'),
                    error = cron.schedule[cronPart].fromString($el.val());
                $el.closest('.control-group').toggleClass('error', !!error)
                    .find('.help-block').text(error||'');
            });
            this.updateAlert();
            this.updateSaveBtn();
        },
        
        updateSaveBtn: function(){
            this.ui.btnSave.attr('disabled', this.cronEnabled && !this.tempCron.isCorrect());
        },

        onSwitchChanged: function(e, data) {
            this.cronEnabled = data.value;
            this.ui.details.toggle(this.cronEnabled);
            this.updateAlert();
            this.updateSaveBtn();
        },
        
        applyEdit: function(){
            var self = this,
                cron = this.model.get('cron'),
                prevValue = cron.toString(),
                errorsHandler = _.bind(this.putErrors, this);;
            cron.fromString(this.cronEnabled ? this.tempCron.toString() : '');
            var xhr = this.model.save(null,{
                validate: false,
                fieldErrorsHandler: errorsHandler,
                globalErrorsHandler: errorsHandler,
                disableControls: this.ui.btnSave
            });
            if (xhr) {
                xhr.done(function(){
                        self.onClose();
                    })
                    .fail(function(){
                        cron.fromString(prevValue);
                    });
            }
        },
        putErrors: function(errors) {
            errors = errors || {};
            errors.globalErrors = errors.globalErrors || [];
            errors.fieldErrors = errors.fieldErrors || {};
            var messages = [].concat(errors.globalErrors).concat(_.values(errors.fieldErrors));
            this.ui.alert.toggle(messages.length > 0)
                .addClass('alert-error')
                .text( messages.join(', ') );

        }
    });
    
    return TaskEditCronView;
});
