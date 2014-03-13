'use strict';

define(function(require) {
    var _ = require('underscore'),
        $ = require('jquery'),
        Marionette = require('marionette'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        SyncList = require('model/sync/sync-list'),
        SyncLayout = require('view/sync/sync-layout'),
        SyncListView = require('view/sync/sync-list-view'),
        PopupDeleteView = require('view/common/popup-delete-view'),
        SyncPopupErrorsView = require('view/sync/sync-popup-errors-view'),
        TaskDescriptor = require('model/sync/task-descriptor'),
        TaskEditCronView = require('view/sync/task-edit-cron-view'),
        TaskEditParametersView = require('view/sync/task-edit-parameters-view');

    // Pause between fetching running tasks
    var RUNNING_INTERVAL = 1000;

    /**
     * Sync page controller.
     * @class
     */
    var SyncController = Marionette.Controller.extend({

        // for debugging
        controllerName: 'SyncController',

        initialize: function() {
            this.modelsParamsLoader = this.loadModelsParams();

            this.sourceList = new SyncList(null, { url: routes.buildUrl('TASKS_DATA') });
            this.runningList = new SyncList(null, { url: routes.buildUrl('TASKS_DATA', 'executing') });
            this.listenTo(this.sourceList, {
                'edit:cron': this.openCronPopup,
                'edit:parameters': this.openParametersPopup,
                'edit:errors': this.openErrorsPopup,
                'expire': this.fetchRunningTasks,
                'sync:delete': this.onSyncDelete
            });
        },

        open: function(region) {
            var self = this;
            // Defer lists creation until model retrieved
            $.when(this.modelsParamsLoader)
                .then(function(){
                    self.layout = new SyncLayout();
                    self.listenTo(self.layout, 'sync:add', self.onSyncAdd);
                    self.sourceListView = new SyncListView({ collection: self.sourceList });
                    region.show(self.layout);
                    self.layout.sourceRegion.show(self.sourceListView);
                    self.refresh();
                });
        },

        refresh: function() {
            var self = this;
            // Defer fetching until model retrieved
            $.when(this.modelsParamsLoader)
                .then(function(){
                    self.sourceList.fetch({ merge: false });
                    self.fetchRunningTasks();
                });
        },
        
        /**
         * Updates list of currently running tasks
         */
        fetchRunningTasks: function(){
            clearTimeout(this.runningTimer);
            var self = this;
            this.runningList.fetch()
                .done(function(){
                    // 1. Update statuses of fetched models
                    self.runningList.each(function(src){
                        var dest = self.sourceList.get(src.id);
                        if (dest) {
                            dest.set(dest.parse(_.pick(src.attributes, 'progress', 'status')));
                        }
                    });
                    // 2. Update statuses for non-fetched, but running
                    var ids = self.runningList.pluck("id");
                    self.sourceList.each(function(model){
                        if (model.isExecuting() && ids.indexOf(model.id) < 0) {
                            model.fetch();
                        }
                    });
                })
                .always(function(){
                     self.runningTimer = setTimeout(_.bind(self.fetchRunningTasks, self), RUNNING_INTERVAL);
                });
        },
        /**
         * Suspends, but does not completely close
         * @override
         */
        close: function() {
            clearTimeout(this.runningTimer);
            this.layout.close();
        },

        loadModelsParams: function (){
            var xhr = this.sync('read', this, { url: routes.absoluteUrl(routes.TASKS_MODELS) });
            xhr.done(function(resp){
                TaskDescriptor.prototype.TASK_TYPES = _.indexBy(resp, 'id');
            });
            return xhr;
        },
        openCronPopup: function(descriptor){
            var popupView = new TaskEditCronView({model: descriptor});
            this.layout.popupsRegion.show(popupView);
        },
        openParametersPopup: function(descriptor){
            var popupView = new TaskEditParametersView({model: descriptor});
            this.layout.popupsRegion.show(popupView);
        },
        openErrorsPopup: function(model){
            var popupView = new SyncPopupErrorsView({model: model});
            this.layout.popupsRegion.show(popupView);
        },

        onSyncAdd: function(type){
            var descriptor = new TaskDescriptor({
                name: type
            });
            this.listenTo(descriptor, 'change', this.onSyncAdded);
            var popupView = new TaskEditParametersView({model: descriptor});
            this.layout.popupsRegion.show(popupView);
        },
        onSyncAdded: function(){
            this.sourceList.fetch({reset:true});
        },
        onSyncDelete: function(model){
            var popupView = new PopupDeleteView({
                model: model,
                confirm: i18n.tc('view.sync.delete.confirm')
            });
            this.layout.popupsRegion.show(popupView);
        }
    });

    return SyncController;

});
