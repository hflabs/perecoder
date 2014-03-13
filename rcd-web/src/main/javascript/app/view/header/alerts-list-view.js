'use strict';

define(function(require) {
    var _ = require('underscore'),
        ListView = require('view/list-view'),
        AlertView = require('view/header/alert-view'),
        viewTemplate = require('text!template/header/alerts.html');

    /**
     * Widget with list of latest run-time alerts.
     * @class
     */
    var AlertsListView = ListView.extend({
        className: 'alerts-container hidden alert-error',
        events: {
            'click .close': 'onCloseClick'
        },
        collectionEvents: {
            'all': 'onCollectionChanged'
        },
        template: _.template(viewTemplate),
        itemView: AlertView,
        itemViewContainer: '.items',

        show: function(){
            this.$el.slideDown('fast');
        },

        hide: function(){
            this.$el.slideUp('fast');
        },

        onCloseClick: function(){
            this.collection.reset();
        },

        onCollectionChanged: function(){
            if (this.collection.length) {
                this.show();
            } else {
                this.hide();
            }
        }

    });

    return AlertsListView;

});
