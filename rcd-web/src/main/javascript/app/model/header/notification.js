'use strict';

/**
 * Notification models.
 */
define([
    'underscore',
    'routes',
    'model/base-model',
    'common/local-date'
], function (_, routes, BaseModel, LocalDate) {

    var NOTIFICATION_TYPES = {
        ERROR: 'ERROR',
        NO_GROUP: 'NO_GROUP',
        NO_DICTIONARY: 'NO_DICTIONARY',
        NO_VALUE: 'NO_VALUE',
        NO_RULE: 'NO_RULE',
        NO_RULE_ALIAS: 'NO_RULE_ALIAS',
        NO_RULE_SET: 'NO_RULE_SET'
    };

    var NOTIFICATION_STATES = {
        PENDING: 'PENDING',
        PROCESSED: 'PROCESSED'
    };

    /**
     * Notification model.
     * @class
     */
    var Notification = BaseModel.extend({

        /**
         * Checks if notification is marked as processed.
         * @returns {boolean} True is notification is processed, false otherwise.
         */
        isProcessed: function() {
            return this.get('processingState') === NOTIFICATION_STATES.PROCESSED;
        },

        /**
         * Url to the recode set.
         * @returns {string}
         */
        recodeUrl: function() {
            var url = '';
            switch (this.get('type')) {
                case NOTIFICATION_TYPES.NO_RULE_SET:
                    var fromDictionary = this.get('fromDictionary');
                    if (fromDictionary && fromDictionary.id) {
                        url = routes.buildUrl('DICTIONARIES', fromDictionary.id);
                    }
                    break;
                case NOTIFICATION_TYPES.NO_RULE:
                case NOTIFICATION_TYPES.NO_VALUE:
                case NOTIFICATION_TYPES.NO_RULE_ALIAS:
                    var rrs = this.get('rrs');
                    if (rrs && rrs.id) {
                        url = routes.buildUrl('RECODES', rrs.id);
                    }
                    break;
            }
            return url;
        },

        /**
         * Marks notification as processed.
         */
        markAsProcessed: function() {
            var self = this,
                xhr = this.save({ processingState: NOTIFICATION_STATES.PROCESSED }, {silent: true});
            if (xhr) {
                xhr.done(function(){
                    self.collection.remove(self);
                });
            }
        },

        /**
         * @protected @override
         */
        parse: function(resp) {
            resp.startDate = new LocalDate(resp.startDate);
            resp.endDate = new LocalDate(resp.endDate);
            return resp;
        }

    });

    Notification.NOTIFICATION_TYPES = NOTIFICATION_TYPES;
    Notification.NOTIFICATION_STATES = NOTIFICATION_STATES;

    return Notification;
});
