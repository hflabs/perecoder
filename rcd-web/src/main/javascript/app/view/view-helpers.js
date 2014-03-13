'use strict';


define(function(require) {
    var _ = require('underscore'),
        routes = require('routes'),
        i18n = require('locale/i18n');

    /**
     * View helpers.
     * @class
     */
    var ViewHelpers = {

        templateHelpers: {
            /**
             * Return localised message.
             * @param {string} text - Message code.
             * @param {number} count - Count for plural.
             * @param {Array} options - Array of additional strings to be inserted into text.
             * @returns {string} Localised message.
             */
            t: function(text, count, options){
                return i18n.t.apply(i18n, arguments);
            },

            /**
             * Returns localized message with capitalized first letter.
             */
            tc: function(text, count){
                return i18n.tc.apply(i18n, arguments);
            },

            /**
             * Returns absolute url for specified relative url
             * @param {string} url
             * @returns {string} absolute url
             */
            absolute: _.bind(routes.absoluteUrl, routes),

            buildUrl: _.bind(routes.buildUrl, routes)
        }

    };

    return ViewHelpers;

});
