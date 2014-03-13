'use strict';

define(function(require) {

    var $ = require('jquery'),
        Marionette = require('marionette'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        RecordModel = require('model/content/record-model'),
        RRSModel = require('model/recodes/rrs-model'),
        MetafieldsList = require('model/content/metafields-list'),
        GridCollection = require('model/grid-collection'),
        RecodesLayout = require('view/recodes/recodes-layout'),
        RRSSettingsPopup = require('view/recodes/rrs-settings-popup'),
        PopupDeleteView = require('view/common/popup-delete-view');

    /**
     * Controller for managing recode rules
     *
     * @class
     */
    var RecodesController = Marionette.Controller.extend({

        // for debugging
        controllerName: 'RecodesController',

        events: {
            'rrs:delete': 'onRRSDelete',
            'recodes:unmatched': 'onRecodesUnmatched',
            'recodes:unselect': 'onRecodesUnselect',
            'recodes:settings': 'onRecodesSettings'
        },

        initialize: function(){
            this.currentRRS = this.createRRS();
            this.fromDictionary = null;
            this.toDictionary = null;
            this.currentRRSLoader = null;

            this.metafields = null;
            this.records = null;
        },

        /* public */

        /**
         * @param region
         * @param {Array} params [rrsId]
         */
        open: function(region, params){
            this.currentRRSLoader = this.loadCurrentRRS(params[0]);
            var self = this;
            $.when(this.currentRRSLoader).then(function(){
                self.fromDictionary = self.currentRRS.get('fromDictionary');
                self.records = self.createRecords();
                self.metafields = self.createMetafields();
                self.layout = self.createLayout();
                region.show(self.layout);
            });
        },

        /**
         * Requests columns and rows for source dictionary
         */
        refresh: function(){
            var self = this;
            $.when(this.currentRRSLoader).then(function(){
                $.when( self.records.fetch(), self.metafields.fetch() ).then(function(){
                    self.onListReady();
                });
            });
        },

        /**
         * Suspends, but does not completely close
         * @override
         */
        close: function(){
            this.layout && this.layout.close();
            this.currentRRS.clear({silent:true});
        },

        /* private */

        createRRS: function(){
            var model = new RRSModel();
            this.listenTo(model, 'change', this.onRRSChanged);
            return model;
        },

        loadCurrentRRS: function(rrsId){
            this.currentRRS.id = rrsId;
            return this.currentRRS.fetch();
        },

        createMetafields: function(){
            var list = new MetafieldsList(null, {dictionaryId: this.fromDictionary.id});
            return list;
        },

        createRecords: function(){
            var list = new GridCollection(null, {
                model: RecordModel,
                url : routes.buildUrl('RECORDS_DATA', this.fromDictionary.id)
            });
            this.listenTo(list, {
                'sync': this.onRecordsSync
            });
            return list;
        },

        /**
         * Creates main layout
         * @return {RecodesLayout}
         */
        createLayout: function () {
            var layout = new RecodesLayout({
                model: this.currentRRS,
                metafields: this.metafields,
                records: this.records
            });
            this.listenTo(layout, {
                'recodes:resize': function($spacer){ this.trigger('recodes:resize', $spacer); },
                'recodes:selected': function(ids){ this.trigger('recodes:select', ids); }
            });
            return layout;
        },

        onListReady: function() {
            this.layout.resizeColumns();
        },

        onRRSChanged: function(){
            this.trigger('recodes:changed', this.currentRRS);
            this.refresh();
        },

        onRecordsSync: function(){
            this.trigger('recodes:from', this.records.pluck('id'));
        },

        /**
         * Handles user's intention to delete current recode-rule-set
         * @param model
         */
        onRRSDelete: function(model){
            var popupDeleteView = new PopupDeleteView({
                model: model,
                confirm: i18n.tc('view.recodes.delete.confirm')
            });
            this.listenTo(model, 'destroy', this.onRRSDeleted);
            this.layout.showPopup(popupDeleteView);
        },

        /**
         * When current recode rule set is deleted, goes to content page
         */
        onRRSDeleted: function(){
            var url = routes.buildUrl('RECORDS', this.fromDictionary.id);
            this.trigger('navigate', url);
        },

        /**
         * Marks records with passed IDs with pin icon
         * @param recordIds
         */
        onRecodesUnmatched: function(recordIds) {
            this.records.each(function(model){
                model.trigger('backgrid:pin', _.indexOf(recordIds, model.id) >= 0);
            });
        },

        onRecodesUnselect: function(){
            this.layout.unselectAllRecords();
        },

        onRecodesSettings: function(toMetafields){
            var view = new RRSSettingsPopup({
                model: this.currentRRS,
                fromMetafields: this.metafields,
                toMetafields: toMetafields
            });
            this.layout.showPopup(view);
        }

    });

    return RecodesController;

});
