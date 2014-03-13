'use strict';

define(function (require) {
    var Marionette = require('marionette'),
        routes = require('routes'),
        DictionaryModel = require('model/dictionaries/dictionary-model'),
        RRSAsideNewGroupsList = require('model/recodes/rrs-aside-new-groups-list'),
        RRSModel = require('model/recodes/rrs-model'),
        RRSAsideNewView = require('view/recodes/rrs-aside-new');

    var NewAsideController = Marionette.Controller.extend({

        // for debugging
        controllerName: 'NewAsideController',

        initialize: function(){
            this.list = new RRSAsideNewGroupsList();
            this.listenTo(this.list, {
                'rrs:create': this.onRRSCreate
            });
            this.currentDictionary = new DictionaryModel();
            this.currentDictionaryLoader = null;
        },

        open: function(region, params){
            this.currentDictionary.id = params[0];
            this.currentDictionaryLoader = this.currentDictionary.fetch();

            var self = this;
            this.currentDictionaryLoader.then(function(){
                self.list.currentDictionaryId = self.currentDictionary.id;
                self.list.currentGroupId = self.currentDictionary.get('groupId');
            });

            this.view = new RRSAsideNewView({ collection: this.list });
            this.listenTo(this.view, 'aside:previous', function(){ this.trigger('aside:previous'); });
            region.show(this.view);
            this.refresh();
        },

        close: function(){
            this.view && this.view.close();
            this.list.reset();
        },

        refresh: function(){
            var self = this;
            this.currentDictionaryLoader.then(function(){
                self.list.fetch();
            });
        },

        /**
         * Creates new recode-rule-set
         * @param fromDictionaryId
         * @param toDictionaryId
         * @param {jQuery} $el
         */
        onRRSCreate: function(fromDictionaryId, toDictionaryId, $el){
            var model = new RRSModel({
                fromDictionaryId: fromDictionaryId,
                toDictionaryId: toDictionaryId
            });
            this.listenTo(model, 'sync', this.onRRSCreated);
            model.save(null, {
                disableControls: $el
            });
        },

        onRRSCreated: function(model){
            var url = routes.buildUrl('RECODES', model.id);
            this.trigger('navigate', url);
        }

    });

    return NewAsideController;
});