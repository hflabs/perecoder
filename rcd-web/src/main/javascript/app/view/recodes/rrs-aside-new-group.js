'use strict';

define(function(require){
    var _ = require('underscore'),
        Marionette = require('marionette'),
        ListView = require('view/list-view'),
        DictionariesList = require('model/dictionaries/dictionaries-list'),
        RRSAsideNewDictionaryView = require('view/recodes/rrs-aside-new-dictionary'),
        viewTemplate = require('text!template/recodes/rrs-aside-new-group.html');

    /**
     * Individual group widget with dictionaries.
     * @class
     */
    var RRSAsideNewGroupView = ListView.extend({
        tagName: 'li',
        template: _.template(viewTemplate),
        itemView: RRSAsideNewDictionaryView,
        itemViewContainer: '.dictionaries',
        events: {
            'click .expander,.command': 'onClick'
        },
        ui: {
            expander: '.expander'
        },
        modelEvents: {
            'filter': 'onFilter'
        },
        collectionEvents: {
            'sync': 'onDictionariesSync',
            'rrs:create': 'onRRSCreate'
        },

        initialize: function(){
            this.expanded = false;
            this.currentDictionaryId = this.model.get('currentDictionaryId');
            this.collection = new DictionariesList(null,{ forbidden: [this.currentDictionaryId] });
            this.collection.queryParams.groupId = this.model.id;
            this.collection.state.pageSize = -1;
        },

        onDictionariesSync: function(){
            this.$el.toggle(!!this.collection.length);
            this.updateExpander();
        },

        updateExpander: function(){
            this.expanded = !!this.collection.length;
            this.ui.expander.toggleClass('expanded', this.expanded);
        },

        onClick: function(){
            if (this.expanded) {
                this.collection.reset();
                this.updateExpander();
            } else {
                this.collection.fetch();
            }
        },

        onFilter: function(pattern){
            if (this.collection.queryParams.search != pattern) {
                this.collection.queryParams.search = pattern;
                if (pattern) {
                    this.collection.fetch();
                }
                else {
                    this.$el.show();
                    this.collection.reset();
                    this.updateExpander();
                }
            }
        },

        onRRSCreate: function(model, $el){
            this.model.trigger('rrs:create', this.currentDictionaryId, model.id, $el);
        }
    });

    return RRSAsideNewGroupView;

});
