'use strict';

define(function(require) {
    var util = require('util'),
        i18n = require('locale/i18n'),
        routes = require('routes'),
        BaseModel = require('model/base-model'),
        Cron = require('common/cron-util'),
        LocalDate = require('common/local-date'),
        Validator = require('common/validator');

    /**
     * Task descriptor object
     * @class
     */
    var TaskDescriptor = BaseModel.extend({
        /**
         * abstract collection, must be overwritten in SyncController.applyConfig
         */
        TASK_TYPES: {},

        defaults: function() {
            return {
                description: '',
                parameters: {}
            }
        },

        urlRoot: routes.absoluteUrl(routes.TASKS_DATA),
        initialize: function() {
            this.constructor.__super__.initialize.apply(this,arguments);
            this.task_type = this.TASK_TYPES[this.get('name')];
            this.validator = new Validator(this.task_type.fields);
            _.defaults(this.get('parameters'), this.task_type.defaultParameters);
        },

        /**
         * Parses raw hash from server to correct Objects
         *
         * Replace timestamps with Date objects, cron's string with Cron object
         *
         * @param {*} attr  recieved hash of attributes
         * @returns         hash with typed values
         */
        parse: function(attr) {
            attr = _.clone(attr);
            attr.cron = new Cron(attr.cron);
            attr.nextScheduledDate = attr.nextScheduledDate && new LocalDate(attr.nextScheduledDate);
            return attr;
        },
        
        validate: function(attrs){
            return this.validator.validate(attrs);
        }
    });

    return TaskDescriptor;
});
