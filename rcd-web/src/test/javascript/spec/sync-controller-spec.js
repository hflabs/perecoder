'use strict';

define(function(require){
    var _ = require('underscore'),
        $ = require('jasmine_jquery'),
        i18n = require('locale/i18n'),
        SyncController = require('controller/sync-controller'),
        fixtures = require('fixtures');

    describe('Tasks Controller', function(){
        
        beforeEach(function(){

            // Create server
            this.server = fixtures.createServer();

            this.syncController = new SyncController();
            // fetch model
            this.server.respond();
            this.syncController.open(fixtures.fakeRegion);

            this.sources = this.syncController.sourceList;
            this.running = this.syncController.runningList;
            // fetch sources
            this.server.respond();
            // fetch executing
            this.server.respond();

            this.server.requests = [];
        });

        afterEach(function(){
            this.server.restore();
            this.syncController.close();
        });

        it('creates models and views', function(){
            expect(this.sources).toBeDefined();
            expect(this.running).toBeDefined();
            expect(this.sources.models.length).toEqual(3);
            expect(this.running.models.length).toEqual(0);
        });

        it('task #1 started', function(){
            var model = this.sources.models[0];
            model.toggleExecute(true);
            expect(this.server.requests.length).toEqual(1);
            this.server.respond();

            expect(model.get('status')).toEqual('RUNNING');

            // runningList's fetch has been run on sync:started
            this.syncController.fetchRunningTasks();
            expect(this.server.requests.length).toEqual(2);
            this.server.respond();
            expect(this.running.models.length).toEqual(1);
            expect(this.running.models[0].get('id')).toEqual(model.get('id'));
        });

        it('task #2 started', function(){
            var model = this.sources.models[1];
            model.toggleExecute(true);
            expect(this.server.requests.length).toEqual(1);
            this.server.respond();

            expect(model.get('status')).toEqual('RUNNING');

            // runningList's fetch has been run on sync:started
            this.syncController.fetchRunningTasks();
            expect(this.server.requests.length).toEqual(2);
            this.server.respond();
            expect(this.running.models.length).toEqual(2);
            expect(this.running.where({id: model.get('id')}).length).toEqual(1);
        });
            
        it('task #1 canceled', function(){
            var model = this.sources.models[0];
            model.toggleExecute(false);
            expect(this.server.requests.length).toEqual(1);
            this.server.respond();

            expect(model.get('status')).toEqual('INTERRUPTING');

            // runningList's fetch has been run on sync:canceled
            this.syncController.fetchRunningTasks();
            expect(this.server.requests.length).toEqual(2);
            this.server.respond();
            expect(this.running.models.length).toEqual(2);
            expect(this.running.where({id: model.get('id')}).length).toEqual(1);
        });

        it('task #2 canceled', function(){
            var model = this.sources.models[1];
            model.toggleExecute(false);
            expect(this.server.requests.length).toEqual(1);
            this.server.respond();

            expect(model.get('status')).toEqual('INTERRUPTING');

            // runningList's fetch has been run on sync:canceled
            this.syncController.fetchRunningTasks();
            expect(this.server.requests.length).toEqual(2);
            this.server.respond();
            expect(this.running.models.length).toEqual(2);
            expect(this.running.where({id: model.get('id')}).length).toEqual(1);
        });

        it('stopped after interrupting', function(){
            expect(this.sources.where({status:'INTERRUPTING'}).length).toEqual(2);
            expect(this.running.where({status:'INTERRUPTING'}).length).toEqual(2);

            // Emulates stopping
            _.each(fixtures.tasks.data, function(source, i){
                fixtures.tasks.data[i].status = 'READY';
            });

            this.syncController.fetchRunningTasks();
            this.server.respond();

            expect(this.running.models.length).toEqual(0);
            // fetching 2 models for main list
            this.server.respond();
            this.server.respond();
            expect(this.sources.where({status:'READY'}).length).toEqual(this.sources.length);
            expect(this.server.requests.length).toEqual(3);
        });
        
    });

});
