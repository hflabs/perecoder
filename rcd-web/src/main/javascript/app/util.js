/**
 * Utility classes and functions.
 */
'use strict';

define([
    'underscore'
], function (_) {

    var util = {};

    /**
     * Capitalizes the first letter of a string.
     * @param string
     * @returns {string}
     */
    util.capitalize = function(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    };

    /**
     * Executes function by its name.
     * @param functionName
     * @param context
     * @returns {*}
     */
    util.execute = function(functionName, context /*, args */) {
        var args = Array.prototype.slice.call(arguments, 2);
        var namespaces = functionName.split(".");
        var func = namespaces.pop();
        for (var i = 0; i < namespaces.length; i++) {
            context = context[namespaces[i]];
        }
        return context[func].apply(context, args);
    };

    return util;

});
