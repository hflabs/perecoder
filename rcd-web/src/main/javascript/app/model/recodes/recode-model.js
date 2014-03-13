'use strict';

define(function(require) {

    var i18n = require('locale/i18n'),
        routes = require('routes'),
        BaseModel = require('model/base-model');

    var RecodeModel = BaseModel.extend({
        defaults: {
//            fromRecordId: null,
            toRecordId: null
        },
        idAttribute: 'fromRecordId'
    });

    return RecodeModel;

});