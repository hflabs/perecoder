'use strict';

define(function(require){

    var _ = require('underscore'),
        i18n = require('locale/i18n'),
        LocalDate = require('common/local-date'),
        SyncSource = require('model/sync/sync-source'),
        SyncSourceView = require('view/sync/sync-source-view'),
        TaskDescriptor = require('model/sync/task-descriptor'),
        fixtures = require('fixtures');

    TaskDescriptor.prototype.TASK_TYPES = _.indexBy(fixtures.tasks.models, 'id');

    describe('SyncSource View', function(){

        var model, descriptorModel, view;
        var baseDate;
        
        var SECOND = 1000;
        var MINUTE = 60 * SECOND;
        var HOUR = 60 * MINUTE;
        var DAY = 24 * HOUR;
        var YEAR = 365 * DAY;
        
        beforeEach(function(){
            baseDate = new Date().valueOf();
            model = new SyncSource({
                id: '06d9fc7b-9fd3-4b84-bf68-b901ac44340d',
                descriptor: {
                    id: '06d9fc7b-9fd3-4b84-bf68-b901ac44340d',
                    name: 'cnsiSyncTaskPerformer',
                    cron: '* * * * * *',
                    description: 'CNSI descriptor',
                    nextScheduledDate: baseDate,
                    parameters: {
                    groupName: 'MyCRM',
                        host: 'localhost',
                        port: 80,
                        timeout: 0,
                        protocol: 'http',
                        path: ''
                    }
                },
                status: 'READY'
            }, {parse: true});
            descriptorModel = model.get('descriptor');
            view = new SyncSourceView({ model: model});
        });

        it('created', function(){
            expect(model).toBeDefined();
            expect(descriptorModel).toBeDefined();
            expect(descriptorModel.get('nextScheduledDate')).toEqual(jasmine.any(LocalDate));
            expect(view).toBeDefined();
        });

        describe('shows correct next sync messages for', function(){

            it('year or more', function(){
                expect(view.nextSync(new Date(baseDate + YEAR + DAY)))
                    .toEqual(i18n.t('model.time.in_infinite'));
            });

            it('few months', function(){
                // 77 days = 2.48 months = 2 month
                expect(view.nextSync(new Date(baseDate + 77 * DAY)))
                    .toEqual(i18n.t('model.time.in_month', 2, [2]));
                // 78 days = 2.51 months = 3 month
                expect(view.nextSync(new Date(baseDate + 78 * DAY + MINUTE)))
                    .toEqual(i18n.t('model.time.in_month', 3, [3]));
            });

            it('few weeks', function(){
                // 17 days = 2 weeks
                expect(view.nextSync(new Date(baseDate + 17 * DAY)))
                    .toEqual(i18n.t('model.time.in_week', 2, [2]));
                // 18 days = 3 weeks
                expect(view.nextSync(new Date(baseDate + 18 * DAY)))
                    .toEqual(i18n.t('model.time.in_week', 3, [3]));
                // 8 days = 1 week
                expect(view.nextSync(new Date(baseDate + 8 * DAY)))
                    .toEqual(i18n.t('model.time.in_week', 1, [1]));
            });

            it('few days, less than week', function(){
                // 6 days 23 hours = 7 days
                expect(view.nextSync(new Date(baseDate + 6 * DAY + 23 * HOUR)))
                    .toEqual(i18n.t('model.time.in_day', 7, [7]));
                // 1 day 1 min = 1 day
                expect(view.nextSync(new Date(baseDate + 1 * DAY + 1 * MINUTE)))
                    .toEqual(i18n.t('model.time.in_day', 1, [1]));
            });

            it('few hours, less than day', function(){
                // 23 hours 59 min = 24 hours
                expect(view.nextSync(new Date(baseDate + 23 * HOUR + 59 * MINUTE)))
                    .toEqual(i18n.t('model.time.in_hour', 24, [24]));
                // 1 hour 1 minuute = 1 hour
                expect(view.nextSync(new Date(baseDate + 1 * HOUR + 1 * MINUTE)))
                    .toEqual(i18n.t('model.time.in_hour', 1, [1]));
            });

            it('few minutes, less than hour', function(){
                // 59 min 59 sec = 60 min
                expect(view.nextSync(new Date(baseDate + 59 * MINUTE + 59 * SECOND)))
                    .toEqual(i18n.t('model.time.in_minute', 60, [60]));
                // 30 min 31 sec = 31 min
                expect(view.nextSync(new Date(baseDate + 30 * MINUTE + 31 * SECOND)))
                    .toEqual(i18n.t('model.time.in_minute', 31, [31]));
                // 1 min 1 sec = 1 min
                expect(view.nextSync(new Date(baseDate + 1 * MINUTE + 1 * SECOND)))
                    .toEqual(i18n.t('model.time.in_minute', 1, [1]));
            });

            it('few seconds, less than minute', function(){
                // 59 sec = 59 sec
                expect(view.nextSync(new Date(baseDate + 59 * SECOND)))
                    .toEqual(i18n.t('model.time.in_second', 59, [59]));
            });

        });
        
        it('fires "expire" event', function(){
            
            // suppose task to be executed rigth now
            descriptorModel.get('nextScheduledDate').date = new Date();
            jasmine.Clock.useMock();
            spyOn(model, 'trigger');
            view.planDateRefresh();
            // emulate REFRESH_DELAY (1000) ms pass
            jasmine.Clock.tick(1000);
            expect(model.trigger).toHaveBeenCalledWith('expire');
        });

    });

});