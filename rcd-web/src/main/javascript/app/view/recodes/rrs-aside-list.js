'use strict';

define(function(require){
    var _ = require('underscore'),
        BaseCollection = require('model/base-collection'),
        Marionette = require('marionette'),
        i18n = require('locale/i18n'),
        RRSListView = require('view/recodes/rrs-list'),
        ViewHelpers = require('view/view-helpers'),
        viewTemplate = require('text!template/recodes/rrs-aside-list.html');

    /**
     * List of recode-sets.
     * @class
     */
    var RRSAsideListView = Marionette.Layout.extend({
        template: _.template(viewTemplate),
        templateHelpers: ViewHelpers.templateHelpers,
        regions: {
            recodesInRegion: '[data-region="recodes-in"]',
            recodesOutRegion: '[data-region="recodes-out"]'
        },
        events: {
            'click [data-action="add"]': 'onAddClick',
            'click .recodes a': 'onClick'
        },
        ui: {
            inHeader: 'h4'
        },
        initialize: function(options){
            this.dictionaryId = options.dictionaryId;
            this.recodesIn = new BaseCollection();
            this.recodesIn.comparator = function(model){
                var dictionary = model.get('fromDictionary');
                return dictionary && dictionary.name;
            };
            this.recodesOut = new BaseCollection();
            this.recodesOut.comparator = function(model){
                var dictionary = model.get('toDictionary');
                return dictionary && dictionary.name;
            };
            this.recodesInView = new RRSListView({ collection: this.recodesIn });
            this.recodesOutView = new RRSListView({ collection: this.recodesOut });
            this.listenTo(this.recodesIn, 'add remove reset', this.onRecodesInChanged);
            this.listenTo(this.collection, {
                'add': this.onRecodesSetAdd,
                'remove': this.onRecodesSetRemove,
                'reset': this.onRecodesSetReset
            });
        },

        onRender: function(){
            this.recodesInRegion.show(this.recodesInView);
            this.recodesOutRegion.show(this.recodesOutView);
        },

        onAddClick: function(){
            this.collection.trigger('rrs:add', this.dictionaryId);
        },

        getRecodeIncome: function(model){
            var income = this.dictionaryId == model.get('toDictionary').id;
            model.set('isIncome', income, { silent: true});
            return income;
        },

        getCollection: function(model){
            return this.getRecodeIncome(model) ? this.recodesIn : this.recodesOut
        },

        onRecodesSetAdd: function(model){
            this.getCollection(model).add(model);
        },

        onRecodesSetRemove: function(model){
            this.getCollection(model).remove(model);
        },

        onRecodesSetReset: function(models){
            this.recodesIn.reset();
            this.recodesOut.reset();
            if (models) {
                _.each(models, function(model){
                    this.getCollection(model).add(model);
                }, this);
            }
        },

        onRecodesInChanged: function(){
            this.ui.inHeader.toggle(this.recodesIn.length > 0);
        },

        onClick: function(e){
            e.preventDefault();
            var url = $(e.currentTarget).attr('href');
            this.collection.trigger('rrs:navigate', url);
        }

    });

    return RRSAsideListView;

});
