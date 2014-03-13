'use strict';

define(function(require) {

    var _ = require('underscore'),
        $ = require('jquery'),
        Marionette = require('marionette'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        RecordModel = require('model/content/record-model'),
        MetafieldModel = require('model/content/metafield-model'),
        MetafieldsList = require('model/content/metafields-list'),
        DictionaryModel = require('model/dictionaries/dictionary-model'),
        DictionaryModelUpload = require('model/dictionaries/dictionary-model-upload'),
        GroupModel = require('model/groups/group-model'),
        RecordsList = require('model/content/records-list'),
        ContentLayout = require('view/content/content-layout'),
        PopupDeleteView = require('view/common/popup-delete-view'),
        PopupUploadView = require('view/dictionaries/dictionary-popup-upload-view');

    var DEFAULT_METAFIELD_TYPE = 'STRING';

    /**
     * Controller for managing dictionary content
     *
     * @class
     */
    var ContentController = Marionette.Controller.extend({

        // for debugging
        controllerName: 'ContentController',

        initialize: function(){
            this.currentDictionary = new DictionaryModel();
            this.currentDictionaryLoader = null;

            this.currentGroup = new GroupModel();
            this.currentGroupLoader = null;

            this.metafields = null;

            this.recordsList = null;

            this.modelParamsLoader = this.loadModelParams();
            this.params = null;
        },

        /* public */

        /**
         * @param region
         * @param {Array} params [dictionaryId]
         */
        open: function(region, params){
            this.loadCurrentDictionary(params[0]);
            var self = this;
            $.when(this.modelParamsLoader, this.currentDictionaryLoader).then(function(){
                self.loadCurrentGroup();
                $.when(self.currentGroupLoader).then(function(){
                    self.metafields = self.createMetafields();
                    self.recordsList = self.createList();
                    self.layout = self.createLayout();
                    region.show(self.layout);
                    self.refresh();
                });
            });
        },

        /**
         * Requests columns and rows
         */
        refresh: function(){
            var metafieldsLoader = this.metafields.fetch();
            var self = this;
            $.when(this.modelParamsLoader, this.currentDictionaryLoader, metafieldsLoader).then(function(){
                self.refreshRecords();
            });
        },

        refreshRecords: function(){
            this.listenToOnce(this.recordsList, 'sync', this.onListSync);
            this.recordsList.fetch();
        },

        /**
         * Suspends, but does not completely close
         * @override
         */
        close: function(){
            this.layout && this.layout.close();
            this.resetRecordModelParams();
        },

        /* private */

        /**
         * Returns subset of controller's fields, which will be passed to layout
         * @return {*}
         */
        toJSON: function(){
            return _.pick(this, 'currentDictionary', 'currentGroup', 'metafields', 'recordsList')
        },

        loadCurrentDictionary: function(dictionaryId){
            this.currentDictionary.id = dictionaryId;
            this.currentDictionaryLoader = this.currentDictionary.fetch();
        },

        loadCurrentGroup: function(){
            this.currentGroup.id = this.currentDictionary.get('groupId');
            this.currentGroupLoader = this.currentGroup.fetch();
        },

        loadModelParams: function(){
            var xhr = this.sync('read', this, { url: routes.absoluteUrl(routes.RECORDS_MODELS) });
            var self = this;
            xhr.done(function(resp){
                self.pageConfig = resp;
                var modelParams = _.indexBy(resp, 'id');
                DictionaryModelUpload.prototype.setModelParams(modelParams.UploadDictionaryDescriptor);
                MetafieldModel.prototype.modelParams = modelParams.MetaField;
            });
            return xhr;
        },

        /**
         * Creates records collection
         * @return {RecordsList}
         */
        createList: function(){
            var list = new RecordsList(null, { dictionaryId: this.currentDictionary.id });
            this.listenTo(list, {
                    'invalid': this.onModelInvalid,
                    'backgrid:add': this.onRecordsAdd,
                    'backgrid:apply': this.onRecordsApply,
                    'backgrid:delete': this.onRecordsDelete,
                    'backgrid:restructure': this.onStructureEdit,
                    'backgrid:restructured': this.onStructuredEdited,
                    'backgrid:column:add': this.onColumnAdd,
                    'backgrid:column:delete': this.onColumnDelete
                });
            list.writable = this.currentDictionary.isWritable();
            return list;
        },

        /**
         * Creates main layout
         * @return {ContentLayout}
         */
        createLayout: function () {
            var layout = new ContentLayout({
                model: this
            });
            this.listenTo(layout, {
                'records:upload': this.onRecordsUpload,
                'records:download:error': this.onRecordsDownloadError
            });
            return layout;
        },

        createMetafields: function(){
            var list = new MetafieldsList(null, {dictionaryId: this.currentDictionary.id});
            this.listenTo(list, {
                'add': this.onMetafieldsAdd,
                'remove': this.onMetafieldsRemove,
                'reset': this.onMetafieldsReset,
                'invalid': this.onModelInvalid
            });
            return list;
        },

        onListSync: function() {
            this.layout.resizeColumns();
        },

        ////        Records methods

        onModelInvalid: function(model, error, options){
            this.trigger('invalid', model, error, options);
        },
        onRecordsAdd: function(){
            this.layout.showCreatingRecord();
        },
        onRecordsAdded: function(model){
            this.refreshRecords();
        },
        onRecordsDelete: function(model){
            var popupDeleteView = new PopupDeleteView({
                model: model,
                confirm: i18n.tc('view.records.delete.confirm')
            });
            this.listenToOnce(model, 'destroy', this.onRecordsDeleted);
            this.layout.showPopup(popupDeleteView);
        },
        onRecordsDeleted: function(model){
            this.refreshRecords();
        },
        onRecordsUpload: function(){
            var modelUpload = new DictionaryModelUpload(
                _.extend({
                    dictionaryId: this.currentDictionary.id
                }, this.currentDictionary.attributes)
            );
            this.listenTo(modelUpload, 'sync', this.onRecordsUploaded);
            var popupUploadView = new PopupUploadView({
                model: modelUpload
            });
            this.layout.showPopup(popupUploadView);
        },
        onRecordsUploaded: function(model){
            this.recordsList.resetState();
            this.refresh();
        },
        onRecordsDownloadError: function(){
            this.trigger('alerts:addError', i18n.tc('view.records.download.error'));
        },

        onRecordsApply: function (model, $el) {
            var xhr = model.save(null, {
                    disableControls: $el
                }),
                self = this;
            if (xhr) {
                xhr.done(function () {
                    self.onRecordsAdded(model);
                });
            }
        },

        ////          Metafields methods

        /**
         * When user wants to edit colonms
         */
        onStructureEdit: function(){
            this.layout.showColumnsEditor();
        },

        /**
         * When user finished editing columns
         */
        onStructuredEdited: function(){
            this.layout.hideColumnsEditor();
        },

        onMetafieldsAdd: function(model){
            RecordModel.prototype.modelParams.fields[model.id] = {
                required: model.get('primary'),
                type: model.get('type') || DEFAULT_METAFIELD_TYPE
            };
            RecordModel.prototype.defaults[model.id] = null;
        },

        onMetafieldsRemove: function(model){
            delete RecordModel.prototype.modelParams.fields[model.id];
            delete RecordModel.prototype.defaults[model.id];
        },

        onMetafieldsReset: function(){
            this.resetRecordModelParams();
        },

        resetRecordModelParams: function(){
            RecordModel.prototype.modelParams.fields = {};
            RecordModel.prototype.defaults = {};
        },

        onColumnAdd: function(){
            var newNameBase = i18n.tc('model.metafield') + ' ',
                newName,
                i = 1;
            do {
                newName = newNameBase + i++;
            } while (this.metafields.where({name: newName}).length);
            this.metafields.create({
                name: newName,
                dictionaryId: this.currentDictionary.id
            }, { wait: true });
        },
        onColumnDelete: function(model){
            var metafield = model.get('model');
            if (metafield) {
                var popupDeleteView = new PopupDeleteView({
                    model: metafield,
                    confirm: i18n.tc('view.columns.delete.confirm')
                });
                this.layout.showPopup(popupDeleteView);
            }
        }

    });

    return ContentController;

});
