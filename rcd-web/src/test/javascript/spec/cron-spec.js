'use strict';

define(function(require) {
    var $ = require('jasmine_jquery'),
       i18n = require('locale/i18n'),
       Cron = require('common/cron-util');

    i18n.setLanguage('ru');

    var cron2string = function(cronExpression) {
        var cron = new Cron(cronExpression);
        return cron.describe();
    }

    describe('Cron expression human readable form for', function() {

        it('each N minutes', function () {
            expect(cron2string('* */10 * * * *')).toEqual('каждые 10 минут');
        });
    
        it('each N minutes inside interval', function () {
            expect(cron2string('* */10 0,3-23 * * *')).toEqual('каждые 10 минут в интервале 0:00-0:50, 3:00-23:50');
            expect(cron2string('* */5 14 * * *')).toEqual('каждые 5 минут в интервале 14:00-14:55');
            expect(cron2string('* */5 14,18 * * *')).toEqual('каждые 5 минут в интервале 14:00-14:55, 18:00-18:55');
            expect(cron2string('* 10-30/5 14,18 * * *')).toEqual('каждые 5 минут с :10 по :30 в интервале 14:10-14:30, 18:10-18:30');
        });
    
        it('each N minutes inside minute interval', function () {
            expect(cron2string('* 5-45/5 * * * *')).toEqual('каждые 5 минут с :05 по :45');
        });
    
        it('exact minutes', function () {
            expect(cron2string('* 5,15,30 * * * *')).toEqual('в :05, :15 и :30');
        });
    
        it('exact minutes recurring on hours', function () {
            expect(cron2string('* 15,45 */2 * * *')).toEqual('в :15 и :45 каждые 2 часа');
        });
    
        it('each minute inside interval', function () {
            expect(cron2string('* * 14 * * *')).toEqual('каждую минуту в интервале 14:00-14:59');
            expect(cron2string('* 0-5 14 * * *')).toEqual('каждую минуту в интервале 14:00-14:05');
            expect(cron2string('* 5-15 12-14 * * *')).toEqual('каждую минуту с :05 по :15 в интервале 12:05-14:15');
        });
    
        it('each hour inside interval', function () {
            expect(cron2string('* 0 8-19 * * *')).toEqual('в :00 в интервале 8:00-19:00');
        });
    
        it('each N minutes recurring on days', function () {
            expect(cron2string('* */30 * 2 * *')).toEqual('каждые 30 минут каждое 2 число месяца');
        });
    
        it('exact time', function () {
            expect(cron2string('* 0 12 * * *')).toEqual('в 12:00');
            expect(cron2string('* 15 10 * * *')).toEqual('в 10:15');
        });
    
        it('exact time and weekday', function () {
            expect(cron2string('* 20 13 * * 0')).toEqual('в 13:20 каждый день (если это воскресенье)');
        });
    
        it('exact time, weekday and month', function () {
            expect(cron2string('* 10,44 14-15 * 3 3')).toEqual('в :10 и :44 в интервале 14:10-15:44 каждый день марта (если это среда)');
        });
    
        xit('exact time inside weekday interval', function () {
            expect(cron2string('* 15 10 * * 1-5')).toEqual('в 10:15 с понедельника по пятницу');
        });
    
        it('exact time and day', function () {
            expect(cron2string('* 15 10 15 * *')).toEqual('в 10:15 каждое 15 число месяца');
        });
    
        it('exact time recurring on days', function () {
            expect(cron2string('* 0 12 1-31/5 * *')).toEqual('в 12:00 каждые 5 дней с 1 по 31 число месяца');
        });
    
        it('exact everything', function () {
            expect(cron2string('* 11 11 11 11 *')).toEqual('в 11:11 каждое 11 ноября');
        });
    
 
    });
});
