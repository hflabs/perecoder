'use strict';

define(function(require) {

    var routes = require('routes'),
        BaseCollection = require('model/base-collection'),
        MetafieldModel = require('model/content/metafield-model');

    var MetafieldsList = BaseCollection.extend({
        model: MetafieldModel,

        /**
         *
         * @param options contains dictionaryId
         */
        initialize: function(models, options){
            BaseCollection.prototype.initialize.apply(this, arguments);
            this.dictionaryId = options && options.dictionaryId;
        },
        url: routes.absoluteUrl(routes.METAFIELDS_DATA),

        /**
         * Adds 'dictionaryId' parameter to url
         * @param options
         */
        fetch: function(options){
            if (this.dictionaryId) {
                (options || (options = {})).data = {
                    dictionaryId: this.dictionaryId
                };
            }
            BaseCollection.prototype.fetch.call(this, options);
        }

    });

    return MetafieldsList;
});
