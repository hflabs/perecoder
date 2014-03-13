'use strict';

define(function(require) {

    var _ = require('underscore'),
        i18n = require('locale/i18n'),
        routes = require('routes'),
        BaseModel = require('model/base-model'),
        Validator = require('common/validator');

    var DictionaryModel = BaseModel.extend({
        modelParams: {},
        defaults: {
            name: '',
            description: '',
            groupId: '',
            code: null
        },
        urlRoot: routes.absoluteUrl(routes.DICTIONARIES_DATA),
        initialize: function(){
            this.constructor.__super__.initialize.apply(this, arguments);
            this.validator = new Validator(this.modelParams.fields);
            this.listenTo(this, 'backgrid:set', this.safeSet);
            this.url = this.constructor.__super__.url;
        },

        toJSON: function(){
            var keys = _.keys(this.modelParams.fields);
            return _.pick(this.attributes, keys);
        },

        validate: function(attrs){
            return this.validator.validate(attrs);
        }

    });

    return DictionaryModel;
});
