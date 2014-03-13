'use strict';

define(function (require) {
    var _ = require('underscore'),
        Backbone = require('backbone'),
        Marionette = require('marionette'),
        Message = require('model/common/message'),
        Header = require('model/header/header'),
        HeaderLayout = require('view/header/header-layout'),
        NotificationList = require('model/header/notification-list'),
        AlertsListView = require('view/header/alerts-list-view'),
        NotificationsView = require('view/header/notification-list-view');

    // Timeout between notifications' fetching
    var NOTIFICATIONS_REFRESH_INTERVAL = 10 * 1000;

    /**
     * Header controller.
     *
     * Implements navigation bar and notifications
     * @class
     */
    var HeaderController = Marionette.Controller.extend({

        // for debugging
        controllerName: 'HeaderController',

        events: {
            'alerts:addInfo': 'addInfoAlert',
            'alerts:addError': 'addErrorAlert',
            'navigation:highlight': 'highlight'
        },
        
        initialize: function () {
            this.header = new Header();

            this.notifications = this.createNotifications();

            this.alerts = this.createAlerts();

            this.createViews();

            this.refreshTimer = null;
        },

        onClose: function () {
            clearInterval(this.refreshTimer);
            this.layout.close();
        },

        open: function (region) {
            region.show(this.layout);
            this.layout.notificationsRegion.show(this.notificationsView);
            this.layout.alertsRegion.show(this.alertsView);
            this.refresh();
            this.refreshTimer = setInterval(_.bind(this.refresh, this), NOTIFICATIONS_REFRESH_INTERVAL);
        },

        refresh: function(){
            this.notifications.fetch();
        },

        createNotifications: function(){
            var list = new NotificationList();
            this.listenTo(list, 'all', this.updateNotificationsState);
            return list;
        },

        createAlerts: function(){
            var list = new Backbone.Collection();
            return list;
        },

        createViews: function(){
            this.layout = new HeaderLayout({ model: this.header });

            this.notificationsView = new NotificationsView({ collection: this.notifications });

            this.alertsView = new AlertsListView({ collection: this.alerts });
        },

        // ===== Methods for tabs =====

        highlight: function (tabName) {
            this.layout.highlight(tabName);
        },

        // ===== Methods for Alerts =====

        addAlert: function (message, type) {
            if (_.isArray(message)){
                _.each(message, function(message){
                    this.addAlert(message, type);
                }, this);
                return
            }
            // do not add message, if it already exists in collection
            var alert = this.alerts.findWhere({ text: message });
            if (!alert) {
                alert = new Message({
                    text: message,
                    type: type
                });
                this.alerts.add(alert);
            }
            return alert;
        },

        addInfoAlert: function (message) {
            return this.addAlert(message, Message.MessageTypes.INFO);
        },

        addErrorAlert: function (message) {
            return this.addAlert(message, Message.MessageTypes.ERROR);
        },

        removeAlert: function(model){
            this.alerts.remove(model);
        }

    });

    return HeaderController;

});
