'use strict';

define(function (require) {
    var routes = require('routes'),
        Backbone = require('backbone'),
        RRSModel = require('model/recodes/rrs-model');

    var RRSList = Backbone.Collection.extend({
        model: RRSModel,
        url: routes.absoluteUrl(routes.RRS_DATA)
    });

    return RRSList;
});

