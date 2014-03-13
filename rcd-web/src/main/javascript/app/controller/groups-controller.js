'use strict';

define(function(require) {

    var $ = require('jquery'),
        Marionette = require('marionette'),
        routes = require('routes'),
        GroupModel = require('model/groups/group-model'),
        GroupsList = require('model/groups/groups-list'),
        GroupsLayout = require('view/groups/groups-layout'),
        GroupPopupAddView = require('view/groups/group-popup-add-view');

    /**
     * Controller for managing groups
     * @class
     */
    var GroupsController = Marionette.Controller.extend({

        // for debugging
        controllerName: 'GroupsController',

        initialize: function(){
            this.modelParamsLoader = this.loadModelsParams();

            this.groupsList = new GroupsList();
            this.listenTo(this.groupsList, 'invalid', this.onModelInvalid);
        },

        /* public */

        /**
         * Suspends, but does not completely close
         * @override
         */
        close: function(){
            this.layout.close();
            delete this.groupsList.search;
        },
        open: function(region){
            var self = this;
            $.when(this.modelParamsLoader)
                .then(function(){
                    self.layout = self.createLayout();
                    region.show(self.layout);
                    self.refresh();
                })
        },
        refresh: function(){
            var self = this;
            $.when(this.modelParamsLoader).then(function(){
                self.groupsList.fetch({ reset: true });
            });
        },

        /* private */

        createLayout: function(){
            var layout = new GroupsLayout({ groupsList: this.groupsList });
            this.listenTo(layout, {
                'groups:add': this.onGroupsAdd,
                'groups:filter': this.onGroupsFilter
            });
            return layout;
        },
        loadModelsParams: function(){
            var xhr = this.sync('read', this, { url: routes.absoluteUrl(routes.GROUPS_MODELS) });
            xhr.done(function(resp){
                GroupModel.prototype.modelParams = resp;
            });
            return xhr;
        },
        onGroupsAdd: function(){
            var model = new GroupModel();
            this.listenToOnce(model, 'sync', this.onGroupsAdded);
            var popupAddView = new GroupPopupAddView({ model: model });
            this.layout.showPopup(popupAddView);
        },
        onGroupsAdded: function(model){
            this.trigger('navigate', routes.buildUrl('DICTIONARIES', model.id));
        },
        onModelInvalid: function(model, error, options){
            this.trigger('invalid', model, error, options);
        },
        onGroupsFilter: function(pattern){
            this.groupsList.search = pattern;
            this.refresh();
        }

    });

    return GroupsController;
});
