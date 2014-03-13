'use strict';

define(function(require) {

    var _ = require('underscore'),
        $ = require('jquery'),
        i18n = require('locale/i18n'),
        routes = require('routes'),
        BaseModel = require('model/base-model'),
        Validator = require('common/validator');

    /**
     * Model for uploading dictionaries. Is used to validate fields before sending form
     * @type {*|void|Backbone.Wreqr.Handlers.extend|Backbone.Wreqr.EventAggregator.extend|Marionette.extend|Marionette.Controller.extend}
     */
    var DictionaryModelUpload = BaseModel.extend({
        modelParams: {},
        setModelParams: function(params){
            this.modelParams = $.extend(true, {}, params, {
                fields: {
                    file: {
                        required: true,
                        uploadable: true
                    }
                }
            });
        },
        defaults: function() {
            return _.extend({
                file: '',
                name: '',
                description: '',
                groupId: ''
            }, this.modelParams.defaultParameters);
        },
        urlRoot: routes.absoluteUrl(routes.DICTIONARIES_DATA),
        initialize: function(){
            this.constructor.__super__.initialize.apply(this, arguments);
            this.validator = new Validator(this.modelParams.fields);
        },
        validate: function(attrs){
            return this.validator.validate(attrs);
        }
    });

    return DictionaryModelUpload;
});
