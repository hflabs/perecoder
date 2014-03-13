/**
 * Utility classes and functions.
 */
'use strict';

define([
    'underscore',
    'locale/i18n'
], function (_, i18n) {
    
    /**
     * Date with Russian formatting.
     * @class
     */
    var LocalDate = function(dateMsec) {
        this.date = _.isUndefined(dateMsec) ? new Date() : new Date(dateMsec);
    };

    LocalDate.prototype = {};
    
    // Add to prototype methods from Date
    _.each(['getDate','getDay','getFullYear','getMilliseconds','getMinutes','getSeconds','getTimezoneOffset','getUTCDate',
        'getUTCDay','getUTCHours','getHours','getUTCMilliseconds','getUTCMinutes','getUTCSeconds','setSeconds','setFullYear',
        'setMilliseconds','setTime','setYear','setDate','setUTCDate','setUTCHours','setHours','setUTCMilliseconds',
        'setUTCMinutes','setMinutes','setMonth','setUTCSeconds','setUTCFullYear','setUTCMonth','toGMTString','toLocaleFormat',
        'toLocaleTimeString','toLocaleDateString','toUTCString','getUTCFullYear','getMonth',
        'Date.UTC','getUTCMonth','getTime','valueOf','getYear'], function(name){
        LocalDate.prototype[name] = function(){
            return Date.prototype[name].apply(this.date, arguments);
        };
    });
    
    // Add to prototype custom methods
    _.extend(LocalDate.prototype, {
        toDateString: function(){
            return i18n.t('model.time.date', 1, {day: this.getDate(), mon: this.getMonth()+1, year: this.getFullYear()});
        },
        toTimeString: function(){
            return i18n.t('model.time.time', 1, {h: this.getHours(), m: this.getMinutes(), s: this.getSeconds()});
        },
        toString: function(){
            return i18n.t('model.time.datetime', 1, {
                day: this.getDate(),
                mon: this.getMonth()+1,
                year: this.getFullYear(),
                h: this.getHours(),
                m: this.getMinutes(),
                s: this.getSeconds()
            });
        },
        toJSON: function(){
            return this.valueOf();
        },
        equals: function(otherDate) {
            return this.valueOf() == otherDate.valueOf();
        },
        dateEquals: function(otherDate) {
            return this.getFullYear() == otherDate.getFullYear() &&
                    this.getMonth() == otherDate.getMonth() &&
                    this.getDate() == otherDate.getDate();
        },
        isValid: function(){
            return true;
        }
    });
    
    return LocalDate;

});
