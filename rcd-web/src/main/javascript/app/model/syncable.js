'use strict';

define(['log'], function (log) {

    /**
     * Generic model which can be synced with server.
     * @class
     */
    var Syncable = {

        /**
         * Listen to XHR error events.
         */
        initialize: function() {
            this.on('error', this.triggerError);
        },

        /**
         * Parse server response.
         * If model is a collection, this method is called both for the collection itself (with 'success' flag)
         * and for its child elements (without 'success' flag).
         * @param resp Response from server.
         * @returns {*} Model attributes.
         */
        parse: function(resp) {
            // Top level object should have 'success' attribute on it
            var isTopLevelObject = ('success' in resp);
            // Should parse 'content' attribute of the response for top-level object
            var doParse = null;
            if (isTopLevelObject) {
                doParse = this.doParse || function(resp) { return resp.content; };
                return doParse(resp);
                // Should parse the response itself for child objects
            } else {
                doParse = this.doParse || function(resp) { return resp; };
                return doParse(resp);
            }
        },

        /**
         * Trigger application error event.
         * @param model The model itself.
         * @param resp XHR object.
         */
        triggerError: function(model, resp) {
            var error;
            try {
                error = $.parseJSON(resp.responseText);
                if (error == null) {
                    error = {};
                }
            } catch (ex) {
                error = { errorMessage: resp.statusText, success: false };
            }
            error.statusCode = resp.status;
            log.error('Error syncing model: ' + error);
            this.trigger('rcd:error', error, this);
        }

    };

    return Syncable;
});
