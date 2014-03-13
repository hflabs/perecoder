'use strict';

define(function(require) {
    var _ = require('underscore'),
        $ = require('jquery'),
        i18n = require('locale/i18n'),
        log = require('log');

    /**
     * Built-in rule checkers
     *
     * Each rule checker is an object with function
     * @param {Object} value - value to check
     * @return {Boolean} true if check has passed, false otherwise
     *
     * The order of checkers in important
     */
    var RuleCheckers = [
        {
            name: 'required',
            isValueValid: function(value, ruleValue){
                return (ruleValue !== true) || !(value === undefined || value === null || value.toString().trim() === "");
            }
        },
        {
            name: 'type',
            isValueValid: function(value, ruleValue){
                if (value == null) {
                    return true;
                }
                switch (ruleValue) {
                    case 'STRING':
                        return _.isString(value);
                    case 'NUMBER':
                        if (_.isString(value)) {
                            value = +value;
                        }
                        return _.isNumber(value) && !_.isNaN(value);
                    case 'BOOLEAN':
                        return _.isBoolean(value);
                }
            }
        },
        {
            name: 'uploadable',
            isValueValid: function(value, ruleValue){
                return !ruleValue || /\.(csv|xml)$/i.test(value);
            }
        }
    ];

    /**
     * Model attributes validator
     * @param {Object} rules - hash with attribute name as key and rules array as value
     * @constructor
     */
    var Validator = function(rules) {
        this.rules = rules;
        this.checkers = RuleCheckers;
    };

    _.extend(Validator.prototype, {

        /**
         * Performs attributes validation.
         * @param {Object} attrs - hash of attributes to check. Attributes can be nested.
         * @return {Object} object if any errors found, otherwise null.
         *  {
         *      fieldErrors: {
         *          <attrName>: message,
         *          ...
         *      }
         *  }
         */
        validate: function(attrs) {
            var errors = this.validateHash(attrs, []);
            return this.wrapErrors(errors);
        },

        /**
         * Validates one-level set of attributes. If any is a nested object, recursively checks it's members
         * @param attrs
         * @param parents
         * @return {Object} hash of errors if any occurs
         */
        validateHash: function(attrs, parents){
            var errors = {};
            _.each(attrs, function(attrValue, attrName) {
                if ($.isPlainObject(attrValue)) {
                    _.extend(errors, this.validateHash(attrValue, parents.concat(attrName)));
                } else {
                    var fullAttrName = parents.concat(attrName).join('.');
                    var error = this.validateAttr(fullAttrName, attrValue);
                    if (error) {
                        errors[fullAttrName] = i18n.t('model.validator.' + error);
                    }
                }
            }, this);
            return errors;
        },

        /**
         * Validates single attribute.
         * @param {String} fullAttrName - attribute name.
         * @param {Object} attrValue - attribute value.
         * @returns {String} name of the failed rule (if any), otherwise null/undefined.
         */
        validateAttr: function(fullAttrName, attrValue) {
            var attrRules = this.rules[fullAttrName];
            if (!attrRules) {
                return;
            }
            if (_.result(attrValue, 'isValid') === true) {
                return;
            }
            // find first failing check for the attribute
            var failedChecker = _.find(this.checkers, function(checker) {
                return !_.isUndefined(attrRules[checker.name]) && !checker.isValueValid(attrValue, attrRules[checker.name]);
            }, this);
            return failedChecker && failedChecker.name;
        },

        /**
         * Wrap errors in an object expected by the model.
         * @param {Object} errors - hash with errors.
         * @returns {Object} - error object or null if no errors found.
         */
        wrapErrors: function(errors) {
            if (!_.isEmpty(errors)) {
                return { fieldErrors: errors };
            } else {
                return null;
            }
        }

    });

    return Validator;
});
