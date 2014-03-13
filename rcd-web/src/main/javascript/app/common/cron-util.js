/**
 * Cron and other time-related utilities.
 */
'use strict';

define([
    'underscore',
    'locale/i18n'
], function (_, i18n) {

    var partNames = ['s','m','h','day','mon','dow'];
    
    /**
     * CronPart is a part of cron schedule.
     * @class
     */
    var CronPart = function(name, string) {
        this.name = name;
        this.ranges = [];
        this.isCorrect = false;
        this.minValue = ({'s':0, 'm':0, 'h':0, 'day':1, 'mon':1, 'dow':0})[name];
        this.maxValue = ({'s':59, 'm':59, 'h':23, 'day':31, 'mon':12, 'dow':7})[name];
        this.fromString(string);
    };
    
    /**
     * Parses string expression
     * @param {string} str  component of original cron string (like '*' or '3-6,9-12')
     * @return {string}    error message if something wrong
     */
    CronPart.prototype.fromString = function(str){
        this.isCorrect = false;
        if (!str)
            return i18n.t('model.cron.error.empty');
        var self = this;
        var part = str.split(',');
        var error = _.compact(_.map(part, function(range, i){  // range - '*' or '1' or '5-10' or '*/2'
            if (!range)
                return 'model.cron.error.empty';
            var res = {};
            range = range.split('/');
            if (range.length > 2)
                return 'model.cron.error.manyslashes';
            if (range.length > 1) {
                if (!range[1])
                    return 'model.cron.error.stepNaN';
                res.step = +range[1];
                if (!res.step)
                    return 'model.cron.error.stepNaN';
            }
            if (range[0] !== '*') {
                range = range[0].split('-');
                if (!range[0])
                    return 'model.cron.error.startNaN';
                res.start = +range[0];
                if (isNaN(res.start))
                    return 'model.cron.error.startNaN';
                if (res.start < self.minValue)
                    return 'model.cron.error.tooSmall';
                if (res.start > self.maxValue)
                    return 'model.cron.error.tooBig';
                if (range.length > 1) {
                    if (!range[1])
                        return 'model.cron.error.stopNaN';
                    res.stop = +range[1];
                    if (isNaN(res.stop))
                        return 'model.cron.error.stopNaN';
                    if (res.stop < self.minValue)
                        return 'model.cron.error.tooSmall';
                    if (res.stop > self.maxValue)
                        return 'model.cron.error.tooBig';
                }
            }
            part[i] = _.isEmpty(res) ? null : res;
        }));
        if (error.length)
            return i18n.t(error[0]);

        this.isCorrect = true;
        this.ranges = _.compact(part);
    };
    
    /**
     * Returns component as raw string
     * @return {string}    string '*' or '3-6,9-12'
     */
    CronPart.prototype.toString = function(){
        if (!this.isCorrect)
            return;
        if (this.isEmpty())
            return '*';
        return _.map(this.ranges, function(range){
            var r = [];
            if (_.isUndefined(range.start)) {
                r.push('*');
            } else {
                var rr = [range.start];
                if (!_.isUndefined(range.stop))
                    rr.push(range.stop);
                r.push(rr.join('-'));
            }
            if (!_.isUndefined(range.step)) {
                r.push(range.step)
            }
            return r.join('/');
        }).join();
    };
    
    /**
     * Calculates all available values for this cron component. 
     * @return {array}     Note, that position of every element equals it's value
     */
    CronPart.prototype.getAllValues = function(){
        var self = this,
            available = [];
            
        if (this.ranges.length) {
            _.each(this.ranges, function(range){
                for(var i = _.isUndefined(range.start) ? self.minValue : range.start,
                        l = _.isUndefined(range.stop) ? (_.isUndefined(range.start) ? self.maxValue : range.start) : range.stop;
                    i<=l;
                    i+=range.step||1
                )
                    available[i] = i;
            });
        } else {
            for(var i=self.minValue; i<=self.maxValue; i++)
                available[i] = i;
        }
        return available;
    };

    /**
     * Calculates all available values, spanned to continuous sequences
     * 
     * Return an array of arrays of values. 
     * For example, mask "3-6,15,5-8,12-14" gives [[3,4,5,6,7,8],[12,13,14,15]]
     * 
     * @return {array} 
     */
    CronPart.prototype.getAllRanges = function(){
        return _.map(_.compact(this.getAllValues().join().replace(/^,+/,'').split(/,{2,}/)), function(range){
            return range.split(',')
        });
    };
    
    /**
     * Checks if the component is '*'
     * @return {boolean} 
     */
    CronPart.prototype.isEmpty = function(){
        return this.ranges.length == 0;
    };

    /**
     * Checks if the component is single exact value like '1'.
     * @return {boolean} 
     */
    CronPart.prototype.isExact = function(){
        return this.ranges.length == 1 && !_.isUndefined(this.ranges[0].start) && _.isUndefined(this.ranges[0].stop);
    };
    
    /**
     * Checks if the component is a single range with step only, like '* / 2'
     * @returns {boolean} 
     */
    CronPart.prototype.isStep = function(){
        return this.ranges.length == 1 && _.isUndefined(this.ranges[0].start) && !_.isUndefined(this.ranges[0].step); 
    };
    
    /**
     * Proxies _.map on <code>this.ranges</code>
     * @returns {array} 
     */
    CronPart.prototype.map = function(callback){
        return _.map(this.ranges, callback);
    };


    
    /**
     * Cron schedules.
     * @class
     * @param {string} string   textual cron expression to initialize new object
     */
    var Cron = function(string) {
        var self = this;
        this.schedule = {};
        _.each(partNames, function(n) {
            self.schedule[n] = new CronPart(n);
        });
        this.fromString(string);
    };

    
    /**
     * Parses string expression to inner representation
     * @param {string} cronstring   cron expression '0 0 18 * * 1-5/2'
     */
    Cron.prototype.fromString = function(cronstring) {
        var self = this;
        var parts = (cronstring || '').split(/\s+/);
        _.each(partNames, function(partName, i) {
            var part = '' + parts[i];
            self.schedule[partName].fromString(part);
        });
    };

    /**
     * Checks if all components have correct values
     * @return {boolean}
     */
    Cron.prototype.isCorrect = function(){
        return _.every(this.schedule, function(cp){
            return cp.isCorrect;
        });
    };
    
    /**
     * Stub to pass model validation
     * @return {boolean}
     */
    Cron.prototype.isValid = function(){
        return true;
    };

    /**
     * Makes string representation
     * @return {string} - eg.: '0 3 * * 6' or undefined
     */
    Cron.prototype.toString = function(){
        var sched = this.schedule,
            parts;
        if (this.isCorrect()) {
            return _.map(partNames, function(partName){
                return sched[partName].toString();
            }).join(' ');
        }
        return '';
    };
    
    /**
     * Serializes data for saving
     */
    Cron.prototype.toJSON = function(){
        return this.toString() || null;
    };
    
    /**
     * Returns human-readable (localized) description for the schedule
     * Doesn't include seconds
     * @return {string}
     */
    Cron.prototype.describe = function(){
        if (!this.isCorrect()) {
            return i18n.t('model.cron.undefined');
        }

        // Joins array, but uses different separator before last element
        function _smartJoin(list, sep, lastsep){
            var last = list.pop();
            return _.compact([list.join(sep||', '), last]).join(lastsep||i18n.t('model.cron.and'));
        }

        function _twoDigit(n) {
            return (n < 10 ? '0' : '') + +n;
        }

        var self = this,
            result = [];

        // 1. Message for time
        if ( this.schedule.m.isExact() && this.schedule.h.isExact() ) {
            // exact time
            result.push(i18n.t('model.cron.at') + this.schedule.h + ':' + _twoDigit(this.schedule.m));
        } else {
        
            // 1.1 Message for minutes
            var minutes = [];
            if (this.schedule.m.isEmpty()) {
                minutes.push(i18n.t('model.cron.every4minute', 1));
            } else {
                var minutes_single = [];
                _.each(this.schedule.m.ranges, function(range){
                    if (!_.isUndefined(range.start) && _.isUndefined(range.stop)) {
                        // single value - 'в :05'
                        minutes_single.push( i18n.t('model.cron.at_minute', 1, [_twoDigit(range.start)]) );
                    } else {
                        // for '*' or range - 'каждые 5 минут'
                        var desc = [];
                        desc.push( i18n.t('model.cron.every4minute', +range.step || 1, [+range.step || 1]) );
                        if (!_.isUndefined(range.start)) {
                            // for ranges, append 'с :01 по :30'
                            if (!self.schedule.h.isExact()) {
                                // but only if hours not single !!!
                                desc.push( i18n.t('model.cron.range_minute', 1, [_twoDigit(range.start), _twoDigit(range.stop)]) );
                            }
                        }
                        minutes.push(desc.join(' '));
                    }
                });
                // if single minutes found, join them as a range
                if (minutes_single.length) {
                    minutes.unshift( i18n.t('model.cron.at') + _smartJoin(minutes_single) );
                }
            }
            result.push(_smartJoin(minutes));
            
            // 1.2 Message for hours
            if (this.schedule.h.isEmpty()) {
                // for empty hours do nothing
            } else if (this.schedule.h.isStep()) {
                // for '*/2' - "каждые 2 часа"
                var step = this.schedule.h.ranges[0].step;
                result.push(i18n.t('model.cron.every4hour', step, [step]));
            } else {
                // get first and last available minutes
                var minutes = _.keys(this.schedule.m.getAllValues());
                minutes = {first: _twoDigit(minutes[0]), last: _twoDigit(minutes[minutes.length-1])};
                // for other complex cases
                var hours = _.map(this.schedule.h.getAllRanges(), function(r) {
                    return i18n.t('model.cron.range_hour', 1, [r[0], minutes.first, r[r.length-1], minutes.last]);
                });
                result.push(i18n.t('model.cron.interval') + ' ' + hours.join(', '));
            }
        }

        // 2. Message for days
        if ( this.schedule.day.isEmpty() ) {
            // Neighter 'days' nor 'dows' specified, do nothing
            if ( !this.schedule.mon.isEmpty() || !this.schedule.dow.isEmpty() ) {
                // Thought if month specified, 'каждый день'
                result.push(i18n.t('model.cron.everyday_of_month'));
            }
        } else if ( this.schedule.day.isExact() ) {
            // for exact day and month
            result.push(i18n.t('model.cron.every4day'));
            result.push(this.schedule.day.ranges[0].start);
            if ( this.schedule.mon.isEmpty() ) {
                result.push(i18n.t('model.cron.day'));
            }
        } else {
            // for other cases
            var days = this.schedule.day;
            var days_single = [], days_range = [];
            _.each(days.ranges, function(range){
                var desc = [];
                if (_.isUndefined(range.start) || !_.isUndefined(range.stop)) {
                    // for '*' or ranges
                    if (range.step)
                        desc.push(i18n.t('model.cron.step', range.step, [range.step]));

                    desc.push(i18n.t('model.cron.range', 1, _.isUndefined(range.start)
                                    // for '*', 'с 1 по 31'
                                    ? [days.minValue, days.maxValue]
                                    // for range, 'с 10 по 15'
                                    : [range.start, range.stop]));
                    days_range.push(desc.join(' '));
                } else {
                    // for single days
                    days_single.push(range.start);
                }
            });
            // if single days found, join them as a range
            if (days_single.length) {
                days_range.unshift( i18n.t('model.cron.every4day') + ' ' + _smartJoin(days_single) );
            }
            result.push( days_range.join(', ') + ' ' + i18n.t('model.cron.day', days_range.length) );
        }

        // 3. Message for months
        if ( this.schedule.mon.isEmpty() ) {
            // if month not specified, do nothing
            if (!this.schedule.day.isEmpty()) {
                // except days are specified
                result.push(i18n.t('model.cron.month_*'));
            }
        } else {
            result.push(_smartJoin(this.schedule.mon.map(function(range) {
                return i18n.t('model.cron.month_'+range.start);
            })));
        }

        // 4 Message for day of week
        if ( !this.schedule.dow.isEmpty() ) {
            var dows = this.schedule.dow.getAllRanges();
            dows = _.map(dows, function(range){
                if (range.length == 1) {
                    // single dow
                    return i18n.t('model.cron.dow_'+range[0]);
                } else {
                    // continuous range
                    return i18n.t('model.cron.dow_from_'+range[0]) + ' ' + i18n.t('model.cron.dow_to_'+range[range.length-1]);
                }
            });

            result.push('(' + i18n.t('model.cron.dow_prefix') + _smartJoin(dows, ', ', i18n.t('model.cron.or')) + ')');
        }

        return result.join(' ');
    };


    return Cron;
});
