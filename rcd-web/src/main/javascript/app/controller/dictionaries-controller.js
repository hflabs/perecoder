'use strict';

define(function(require) {

    var _ = require('underscore'),
        $ = require('jquery'),
        Marionette = require('marionette'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        DictionaryModel = require('model/dictionaries/dictionary-model'),
        DictionaryModelUpload = require('model/dictionaries/dictionary-model-upload'),
        GroupsList = require('model/groups/groups-list'),
        DictionariesList = require('model/dictionaries/dictionaries-list'),
        DictionariesLayout = require('view/dictionaries/dictionaries-layout'),
        DictionaryPopupAddView = require('view/dictionaries/dictionary-popup-add-view'),
        PopupDeleteView = require('view/common/popup-delete-view'),
        DictionaryPopupUploadView = require('view/dictionaries/dictionary-popup-upload-view');

    /**
     * Controller for managing dictionaries
     *
     * Generates events:
     *   dictionaries:added - when new dictionary created. Argument 'model' - new dictionary.
     *   dictionaries:deleted - when dictionary have been deleted. Argument 'model' - deleted dictionary.
     *
     * @class
     */
    var DictionariesController = Marionette.Controller.extend({

        // for debugging
        controllerName: 'DictionariesController',

        initialize: function(){
            this.groupsList = new GroupsList();
            this.modelsParamsLoader = this.loadModelsParams();
            this.groupsListLoader = null;
            this.currentGroup = null;

            this.list = this.createList();
        },

        /* public */

        /**
         * Suspends, but does not completely close
         * @override
         */
        close: function(){
            this.layout.close();
            this.list.resetQueryParams();
            this.list.resetState();
        },

        /**
         * @param region
         * @param {Array} params [groupId(optional)]
         */
        open: function(region, params){
            this.region = region;
            this.loadGroups();
            var self = this;
            $.when(this.modelsParamsLoader, this.groupsListLoader).then(function(){
                self.getCurrentGroup(params[0]);
                self.layout = self.createLayout();
                self.region.show(self.layout);
                self.refresh();
            });
        },
        refresh: function(){
            var self = this;
            $.when(this.modelsParamsLoader, this.groupsListLoader).then(function(){
                self.listenToOnce(self.list, 'sync', self.onListSync);
                self.list.fetch({reset: true});
            });
        },

        /* private */

        loadGroups: function(){
            this.groupsListLoader = this.groupsList.fetch();
        },
        loadModelsParams: function(){
            var xhr = this.sync('read', this, { url: routes.absoluteUrl(routes.DICTIONARIES_MODELS) });
            xhr.done(function(resp){
                var modelParams = _.indexBy(resp, 'id');
                DictionaryModel.prototype.modelParams = modelParams.Dictionary;
                DictionaryModelUpload.prototype.setModelParams(modelParams.UploadDictionaryDescriptor);
            });
            return xhr;
        },
        getCurrentGroup: function(groupId){
            this.currentGroup = this.groupsList.get(groupId);
            if (this.currentGroup) {
                this.list.queryParams.groupId = groupId;
            } else {
                delete this.list.queryParams.groupId;
            }
        },

        createList: function(){
            var list = new DictionariesList();
            this.listenTo(list, {
                'invalid': this.onModelInvalid,
                'backgrid:delete': this.onDictionariesDelete,
                'backgrid:row:click': this.onRowClick
            });
            return list;
        },

        createLayout: function () {
            var layout = new DictionariesLayout({
                list: this.list,
                groupsList: this.groupsList,
                currentGroup: this.currentGroup
            });
            this.listenTo(layout, {
                'dictionaries:add': this.onDictionariesAdd,
                'dictionaries:delete': this.onDictionariesDelete,
                'dictionaries:upload': this.onDictionariesUpload
            });
            return layout;
        },

        onListSync: function() {
            this.layout.resizeColumns();
        },
        onModelInvalid: function(model, error, options){
            this.trigger('invalid', model, error, options);
        },
        onDictionariesAdd: function(){
            var model = new DictionaryModel({
                groupId: this.currentGroup && this.currentGroup.id
            });
            this.listenToOnce(model, 'sync', this.onDictionariesAdded);
            var popupAddView = new DictionaryPopupAddView({
                model: model,
                groupsList: this.groupsList
            });
            this.layout.showPopup(popupAddView);
        },
        onDictionariesAdded: function(model){
            if (model && model.id) {
                this.trigger('navigate', routes.buildUrl('RECORDS', model.id));
            } else {
                this.refresh();
            }
        },
        onDictionariesDelete: function(model){
            var popupDeleteView = new PopupDeleteView({
                model: model,
                confirm: i18n.tc('view.dictionaries.delete.confirm')
            });
            this.listenToOnce(model, 'destroy', this.onDictionariesDeleted);
            this.layout.showPopup(popupDeleteView);
        },
        onDictionariesDeleted: function(model){
            this.refresh();
            this.trigger('dictionaries:deleted', model);
        },
        onDictionariesUpload: function(){
            var modelUpload = new DictionaryModelUpload({
                groupId: this.currentGroup && this.currentGroup.id,
                dictionaryId: null
            });
            this.listenTo(modelUpload, 'sync', this.onDictionariesAdded);
            var popupUploadView = new DictionaryPopupUploadView({
                model: modelUpload,
                groupsList: this.groupsList
            });
            this.layout.showPopup(popupUploadView);
        },
        onRowClick: function(model){
            var url = routes.buildUrl('RECORDS', model.id);
            this.trigger('navigate', url);
        }

    });

    return DictionariesController;
});
