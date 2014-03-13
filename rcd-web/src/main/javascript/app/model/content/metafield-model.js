'use strict';

define(function(require) {

    var _ = require('underscore'),
        routes = require('routes'),
        BaseModel = require('model/base-model'),
        Validator = require('common/validator');

    var MetafieldModel = BaseModel.extend({
        modelParams: {
            defaultParameters: {},
            fields: {}
        },
        defaults: function(){
            return _.extend({
                'name': '',
                'description': '',
                'dictionaryId': '',
                'hidden': false,
                'writable': true,
                'primary': false,
                'type': 'STRING',
                'unique': false
            }, this.modelParams.defaultParameters);
        },
        initialize: function(){
            this.constructor.__super__.initialize.apply(this, arguments);
            this.url = BaseModel.prototype.url;
            this.validator = new Validator(this.modelParams.fields);
        },
        urlRoot: routes.absoluteUrl(routes.METAFIELDS_DATA),

        toJSON: function(){
            var keys = _.keys(this.modelParams.fields);
            return _.pick(this.attributes, keys);
        },

        validate: function(attrs){
            return this.validator.validate(attrs);
        }
    });

    return MetafieldModel;
});
