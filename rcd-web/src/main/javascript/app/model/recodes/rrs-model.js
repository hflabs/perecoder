'use strict';

define(function(require) {

    var i18n = require('locale/i18n'),
        routes = require('routes'),
        BaseModel = require('model/base-model');

    var RRSModel = BaseModel.extend({
        defaults: {
            fromDictionaryId: '',
            toDictionaryId: ''
        },
        urlRoot: routes.absoluteUrl(routes.RRS_DATA)
    });

    return RRSModel;

});