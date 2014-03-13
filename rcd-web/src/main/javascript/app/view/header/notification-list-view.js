'use strict';

define(function(require) {
    var _ = require('underscore'),
        i18n = require('locale/i18n'),
        ListView = require('view/list-view'),
        NotificationView = require('view/header/notification-view'),
        viewTemplate = require('text!template/header/notifications.html');

    /**
     * Widget with list of latest notifications.
     * @class
     */
    var NotificationListView = ListView.extend({
        events: {
            'click .notifications-toggle': 'toggle',
            'click .slideup': 'hide',
            'keydown': 'onKeydown'
        },
        collectionEvents: {
            'all': 'onCollectionChanged'
        },
        ui: {
            overlay: '.notifications-overlay',
            btnToggle: '.notifications-toggle',
            list: '.notifications-container',
            count: '.count'
        },
        template: _.template(viewTemplate),
        itemView: NotificationView,
        itemViewContainer: '.items',

        show: function(){
            this.ui.list.slideDown();
            this.ui.overlay.fadeIn();
        },

        hide: function(){
            this.ui.list.slideUp();
            this.ui.overlay.fadeOut();
        },

        onCollectionChanged: function(){
            var counts = this.collection.pluck('count'),
                count = _.reduce(counts, function(c,s){
                    return c + s;
                }, 0);
            if (!count) {
                this.hide();
            }
            this.setCount(count);
        },

        toggle: function(show){
            var isVisible = this.ui.list.is(':visible');
            if (!_.isBoolean(show)) {
                show = !isVisible;
            }
            if (show == isVisible) {
                return;
            }
            if (show) {
                this.show();
            } else {
                this.hide();
            }
        },

        setCount: function(count){
            this.ui.btnToggle.toggle(count > 0);
            this.ui.count.text(i18n.tc('view.notifications.count', count, count));
        },

        onKeydown: function(e){
            var key = e.keyCode || e.which;
            if (key == 27) {
                this.hide();
            }
        }

    });

    return NotificationListView;

});
