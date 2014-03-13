'use strict';

define(function(require) {

    var routes = require('routes'),
        BaseCollection = require('model/base-collection'),
        RecodeModel = require('model/recodes/recode-model');

    var RecodesCollection = BaseCollection.extend({
        model: RecodeModel,

        /**
         *
         * @param options contains rrsId
         */
        initialize: function(models, options){
            BaseCollection.prototype.initialize.apply(this, arguments);
            this.url = routes.buildUrl('RECODES_DATA', options && options.rrsId)
        }

    });

    return RecodesCollection;
});
