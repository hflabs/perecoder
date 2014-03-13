'use strict';

define(function(require) {
    var _ = require('underscore'),
        BaseModel = require('model/base-model'),
        TaskDescriptor = require('./task-descriptor'),
        LocalDate = require('common/local-date');

        /**
         * All available state values
         */
    var STATES = {
        UNKNOWN: 'UNKNOWN',
        READY: 'READY',
        PENDING: 'PENDING',
        RUNNING: 'RUNNING',
        INTERRUPTING: 'INTERRUPTING',
        SKIPPED: 'SKIPPED',
        FINISHED: 'FINISHED',
        ERROR: 'ERROR',
        CANCELED: 'CANCELED'
    };

    var TASK_STATES_EXECUTING = [STATES.PENDING, STATES.RUNNING, STATES.INTERRUPTING];

    /**
     * Sync source description.
     * @class
     */
    var SyncSource = BaseModel.extend({

        initialize: function(){
            this.constructor.__super__.initialize.apply(this,arguments);
            var descriptor = this.get('descriptor');
            if (!descriptor) {
                descriptor = new TaskDescriptor();
                this.set('descriptor', descriptor);
            }
            this.listenTo(descriptor, {
                'change': this.onDescriptorChanged
            });
            // Associate descriptor with collection to let 410 error proceed
            descriptor.collection = this.collection;
        },

        /**
         * Parses raw hash from server to correct Objects
         *
         * Replaces <code>descriptor</code>'s data with <code>TaskDescriptor</code> model,
         * and all timestamps with <code>LocalDate</code> objects
         *
         * @param {*} attr  received hash of attributes
         * @returns         hash with typed values
         */
        parse: function(attr) {
            if (attr.descriptor) {
                var descriptor = this.get('descriptor');
                if (descriptor) {
                    descriptor.set(descriptor.parse(attr.descriptor));
                    attr.descriptor = descriptor;
                } else {
                    attr.descriptor = new TaskDescriptor(attr.descriptor, {parse: true});
                }
            }
            var r = attr.result;
            if (r) {
                r.registrationDate = r.registrationDate && new LocalDate(r.registrationDate);
                r.startDate = r.startDate && new LocalDate(r.startDate);
                r.endDate = r.endDate && new LocalDate(r.endDate);
            }
            return attr;
        },

        toggleExecute: function(execute){
            if (_.isUndefined(execute)) {
                execute = !this.isExecuting();
            }
            var self = this,
                method = execute ? 'execute' : 'cancel';
            this.sync(method, this)
                .done( function(resp){
                    self.set(self.parse(resp));
                });
        },

        onDescriptorChanged: function(){
            this.trigger('change:descriptor');
        },

        isOfStatus: function(status){
            return this.get('status') === status;
        },
        isStatusReady: function(){
            return this.isOfStatus(STATES.READY);
        },
        isStatusInterrupting: function(){
            return this.isOfStatus(STATES.INTERRUPTING);
        },
        isExecuting: function(){
            return TASK_STATES_EXECUTING.indexOf(this.get('status')) >= 0;
        },

        isResultCanceled: function(){
            var result = this.get('result');
            return result && result.status === STATES.CANCELED;
        },
        isResultFinished: function(){
            var result = this.get('result');
            return result && result.status === STATES.FINISHED;
        },
        isResultError: function(){
            var result = this.get('result');
            return result && result.status === STATES.ERROR;
        },
        isResultDictsErrors: function(){
            var result = this.get('result');
            return result && result.content && result.content.errorCount > 0;
        },
        isResultIncomplete: function(){
            return this.isResultFinished() && this.isResultDictsErrors();
        },
        hasErrors: function() {
            return this.isResultError() || this.isResultIncomplete();
        },
        hasMultipleResults: function() {
            var result = this.get('result');
            return result && result.content && result.content.hasOwnProperty('totalCount');
        }
    });

    SyncSource.STATES = STATES;

    return SyncSource;
});
