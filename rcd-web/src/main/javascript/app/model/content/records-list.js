'use strict';

define(function(require) {

    var routes = require('routes'),
        GridCollection = require('model/grid-collection'),
        RecordModel = require('model/content/record-model');

    var RecordsList = GridCollection.extend({
        model: RecordModel,

        /**
         *
         * @param options contains dictionaryId
         */
        initialize: function(models, options){
            GridCollection.prototype.initialize.apply(this, arguments);
            this.url = routes.buildUrl('RECORDS_DATA', options && options.dictionaryId);
        }

    });

    return RecordsList;
});
