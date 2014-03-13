'use strict';

define(function (require) {
    var $ = require('jquery'),
        _ = require('underscore'),
        Marionette = require('marionette'),
        routes = require('routes'),
        EmptyModel = require('model/common/empty-model'),
        BaseCollection = require('model/base-collection'),
        RecodesCollection = require('model/recodes/recodes-collection'),
        RecordsList = require('model/content/records-list'),
        MetafieldsList = require('model/content/metafields-list'),
        RecodeModel = require('model/recodes/recode-model'),
        RRSModel = require('model/recodes/rrs-model'),
        RRSList = require('model/recodes/rrs-list'),
        RecodesAsideView = require('view/recodes/recodes-aside-view');

    var RecodesAsideController = Marionette.Controller.extend({

        // for debugging
        controllerName: 'RecodesAsideController',

        initialize: function(){
            this.currentRRS = new RRSModel();
            this.currentRRSLoader = null;
            this.fromRecordIDs = [];
            this.fromRecordsSelected = [];
            this.recodes = null;
            this.metafields = null;
            this.records = null;
            this.targets = null;
            this.layout = null;
            this.matchedRecords = this.createMatchedRecords();
            this.dropdownRecords = null;
            this.$pendingSpacer = null;
        },

        events: {
            'recodes:from': 'onRecodeFrom',
            'recodes:select': 'onRecodeSelect',
            'recodes:resize': 'onRecodesResize',
            'recodes:changed': 'onRecodesChanged'
        },

        /**
         * @param region
         * @param {Array} params [rrsId]
         */
        open: function(region, params){
            var self = this;
            this.currentRRSLoader = this.loadCurrentRRS(params[0]);
            $.when(this.currentRRSLoader).then(function(){
                self.fromDictionary = self.currentRRS.get('fromDictionary');
                self.toDictionary = self.currentRRS.get('toDictionary');
                self.recodes = self.createRecodes();
                self.metafields = self.createMetafields();
                self.records = self.createRecords();
                self.targets = self.createTargets();
                self.dropdownRecords = self.createDropdownRecords();
                self.layout = self.createLayout();
                region.show(self.layout);
                self.layout.resizeSpacers(self.$pendingSpacer);
                self.refresh();
            });
        },

        refresh: function(){
            var self = this;
            $.when(this.currentRRSLoader).then(function(){
                self.targets.fetch({
                    data: {
                        dictionaryId: self.fromDictionary.id,
                        direction: 'FROM'
                    }
                });
                if (self.fromRecordIDs.length) {
                    self.recodes.fetch({
                        data: {
                            recordIDs: self.fromRecordIDs.join()
                        }
                    });
                } else {
                    self.onRecodesUpdated();
                }
            });
        },

        onRecodesUpdated: function(){
            var toRecordsNeeded = this.recodes.pluck('toRecordId');
            toRecordsNeeded.push(this.currentRRS.get('defaultRecordId'));
            $.when( this.metafields.fetch(), this.records.fetch({
                    data: { recordIDs: _.compact(_.unique(toRecordsNeeded)).join() }
                }) ).then(_.bind(this.onListReady, this));
        },

        close: function(){
            if (this.layout) {
                this.layout.close();
                this.layout = null;
            }
            this.matchedRecords.reset();
        },

        loadCurrentRRS: function(rrsId){
            this.currentRRS.id = rrsId;
            return this.currentRRS.fetch();
        },

        createRecodes: function(){
            var list = new RecodesCollection(null, { rrsId: this.currentRRS.id });
            this.listenTo(list, 'sync reset', this.onRecodesUpdated);
            return list;
        },

        createMetafields: function(){
            var list = new MetafieldsList(null, { dictionaryId: this.toDictionary.id });
            return list;
        },

        createRecords: function(){
            var list = new RecordsList(null, { dictionaryId: this.toDictionary.id });
            this.listenTo(list, 'reset', this.updateMatchedRecords);
            return list;
        },

        createTargets: function(){
            var list = new RRSList();
            list.currentId = this.toDictionary.id;
            list.comparator = function(model){
                var dictionary = model.get('toDictionary');
                return dictionary && dictionary.name;
            };
            this.listenTo(list, 'rrs:navigate', this.onRRSNavigate);
            return list;
        },

        createDropdownRecords: function(){
            var list = new RecordsList(null, { dictionaryId: this.toDictionary.id });
            this.listenTo(list, {
                'recordselector:selected': this.onRecordsSelectorSelected
            });
            return list;
        },

        createMatchedRecords: function(){
            var list = new BaseCollection();
            this.listenTo(list, {
                'recordselector:select': this.onRecordsSelectorSelect,
                'recordselector:unselect': this.onRecordsSelectorUnselect
            });
            return list;
        },

        createLayout: function(){
            var view = new RecodesAsideView({
                model: this.currentRRS,
                metafields: this.metafields,
                targets: this.targets,
                records: this.matchedRecords,
                dropdownRecords: this.dropdownRecords
            });
            this.listenTo(view, {
                'aside:new': this.onAsideNew,
                'aside:done': this.onAsideDone,
                'rrs:delete': this.onRRSDelete,
                'rrs:settings': this.onRRSSettings,
                'recodes:select': this.onRecodeSelect
            });
            return view;
        },

        /**
         * Creates list of records to be shown in right table
         *
         * To allow duplicates, changes 'id' attribute name to '_id'
         */
        updateMatchedRecords: function(){
            var defaultRecordId = this.currentRRS.get('defaultRecordId'),
                matchedDefault = [];
            var toRecords = _.map(this.fromRecordIDs, function(fromId){
                var recode = this.recodes.findWhere({fromRecordId: fromId}),
                    toRecordId;
                if (recode) {
                    toRecordId = recode.get('toRecordId');
                } else {
                    if (defaultRecordId) {
                        matchedDefault.push(fromId);
                    }
                    toRecordId = defaultRecordId;
                }

                var toRecord = this.records.get(toRecordId);
                if (toRecord) {
                    var attrs = _.clone(toRecord.attributes);
                    attrs.fromRecordId = fromId;
                    attrs._id = attrs.id;
                    delete attrs.id;
                    return attrs;
                } else {
                    return new EmptyModel({fromRecordId: fromId});
                }
            }, this);

            this.matchedRecords.reset(toRecords);
            this.trigger('recodes:unmatched', matchedDefault);
        },

        /**
         * Tells layout that destination records are loaded (invoked only once)
         */
        onListReady: function(){
            this.layout.resizeColumns();
        },

        /**
         * Recieves records ids from <code>RecodesController</code>
         * @param ids
         */
        onRecodeFrom: function(ids){
            this.fromRecordIDs = ids;
            this.refresh();
        },

        /**
         * Recieves IDs of selected records in <code>RecodesController</code>
         * @param ids
         */
        onRecodeSelect: function(ids){
            if (_.isArray(ids)) {
                this.fromRecordsSelected = ids;
                if (ids.length) {
                    this.layout.showDropdown(ids);
                } else {
                    this.layout.hideDropdown();
                }
            }
        },

        onRecodesResize: function($spacer){
            if (this.layout) {
                this.layout.resizeSpacers($spacer);
            } else {
                this.$pendingSpacer = $spacer;
            }
        },

        /**
         * Triggered when recode-rule-set settings changed
         * @param {RRSModel} model
         */
        onRecodesChanged: function(model){
            this.currentRRS.set(model.attributes);
        },

        /**
         * Handles user's intention to create new recode-rule-set
         */
        onAsideNew: function(){
            var fromDictionaryId = this.currentRRS.get('fromDictionary').id;
            this.trigger('aside:new', fromDictionaryId);
        },

        /**
         * Close recodes editing and return to dictionary content page
         */
        onAsideDone: function(){
            var fromDictionaryId = this.currentRRS.get('fromDictionary').id;
            var url = routes.buildUrl('RECORDS', fromDictionaryId);
            this.trigger('navigate', url);
        },

        /**
         * Switches to another recode-rule-set of current dictionary
         * @param {RRSModel} model
         */
        onRRSNavigate: function(model){
            var url = routes.buildUrl('RECODES', model.id);
            this.trigger('navigate', url);
        },

        /**
         * Passes user's intention to delete current recode-rule-set to <code>RecodesController</code>
         */
        onRRSDelete: function(){
            this.trigger('rrs:delete', this.currentRRS);
        },

        /**
         * Handles user's intention to open recode-rule-set's settings
         */
        onRRSSettings: function(){
            this.trigger('recodes:settings', this.metafields);
        },

        /**
         * User clicks on table row
         * @param {RecordModel} model
         */
        onRecordsSelectorSelect: function(model){
            var ids = [model.get('fromRecordId')];
            this.onRecodeSelect(ids);
        },

        onRecordsSelectorUnselect: function(){
            this.trigger('recodes:unselect');
        },

        /**
         * Sets recode rule
         *
         * @param id    target-record, bay me null
         */
        onRecordsSelectorSelected: function(id){
            var recode = new RecodeModel({
                fromRecordIDs: this.fromRecordsSelected,
                toRecordId: id
            }, { url: routes.buildUrl('RECODES_DATA', this.currentRRS.id) }),
                self = this;
            recode.save().done(function(resp){
                if (id) {
                    self.recodes.set(resp, {remove: false});
                } else {
                    var idsToRemove = _.pluck(resp, 'fromRecordId');
                    self.recodes.remove(idsToRemove);
                }
                self.recodes.trigger('reset');
                self.trigger('recodes:unselect');
            });
        }

    });

    return RecodesAsideController;
});