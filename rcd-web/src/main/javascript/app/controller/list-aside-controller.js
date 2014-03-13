'use strict';

define(function (require) {
    var Marionette = require('marionette'),
        routes = require('routes'),
        RRSList = require('model/recodes/rrs-list'),
        RRSAsideListView = require('view/recodes/rrs-aside-list');

    var ListAsideController = Marionette.Controller.extend({

        // for debugging
        controllerName: 'ListAsideController',

        initialize: function(){
            this.list = new RRSList();
            this.listenTo(this.list, {
                'rrs:add': this.onRRSAdd,
                'rrs:navigate': this.onRRSNavigate
            });
            this.currentDictionaryId = null;
            this.view = null;
        },

        open: function(region){
            this.currentDictionaryId = routes.extractParam(location.href);
            this.view = new RRSAsideListView({
                collection: this.list,
                dictionaryId: this.currentDictionaryId
            });
            region.show(this.view);
            this.refresh();
        },

        refresh: function(){
            this.list.fetch({ data: { dictionaryId: this.currentDictionaryId} });
        },

        close: function(){
            this.view && this.view.close();
            this.list.reset();
        },

        /**
         * Handles user request for adding new recode-set
         */
        onRRSAdd: function(dictionaryId){
            this.trigger('aside:new', dictionaryId);
        },

        /**
         * Handles user request for editing rules of recode-rule-set
         */
        onRRSNavigate: function(url){
            this.trigger('navigate', url);
        }

    });

    return ListAsideController;
});