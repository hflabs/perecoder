'use strict';

define(function(require) {

    var _ = require('underscore'),
        BaseModel = require('model/base-model'),
        Validator = require('common/validator');

    var RecordModel = BaseModel.extend({
        defaults: {},
        modelParams: {
            fields: {}
        },
        initialize: function(){
            this.constructor.__super__.initialize.apply(this, arguments);
            this.listenTo(this, 'backgrid:set', this.safeSet);
            this.validator = new Validator(this.modelParams.fields);
            this.url = this.constructor.__super__.url;
        },

        toJSON: function(){
            var keys = _.keys(this.modelParams.fields),
                attrs = _.pick(this.attributes, keys);
            return _.defaults(attrs, this.defaults);
        },

        validate: function(attrs){
            return this.validator.validate(attrs);
        }
    });

    return RecordModel;
});
