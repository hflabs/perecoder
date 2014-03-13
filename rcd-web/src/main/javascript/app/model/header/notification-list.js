'use strict';

/**
 * Notification models.
 */
define([
    'routes',
    'model/base-collection',
    'model/header/notification'
], function (routes, BaseCollection, Notification) {

    /**
     * List of notifications.
     * @class
     */
    var NotificationList = BaseCollection.extend({
        model: Notification,

        url: routes.absoluteUrl(routes.NOTIFICATIONS_DATA),

        initialize: function() {
            this.constructor.__super__.initialize.apply(this);
        },

        /**
         * Newest notification date.
         * @returns {Date} Newest end date across all notifications.
         */
        maxDate: function() {
            if (this.length == 0) {
                return null;
            }
            return this.at(this.length-1).get('endDate');
        },

        /**
         * Oldest notification date.
         * @returns {Date} Oldest start date across all notifications.
         */
        minDate: function() {
            if (this.length == 0) {
                return null;
            }
            return this.at(0).get('startDate');
        },

        /**
         * Overall time interval in hours.
         * @returns {number} Time interval occupied by notifications (in hours).
         */
        interval: function() {
            if (this.length == 0) {
                return 0;
            }
            return this.maxDate().getHours() - this.minDate().getHours() + 1;
        }

    });

    return NotificationList;
});
