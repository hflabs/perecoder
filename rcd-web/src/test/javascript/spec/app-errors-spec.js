'use strict';

define(function(require){
    var i18n = require('locale/i18n'),
        AppController = require('controller'),
        fixtures = require('fixtures');

    describe('App handles sync errors', function(){
        
        beforeEach(function(){
            this.server = fixtures.createServer();

            this.appc = new AppController();

            this.appc.createHeader();
            this.appc.showTasks();

            // respond with page model
            this.server.respond();

            this.sources = this.appc.syncPageController.sourceList;

            this.server.respond();
            this.server.respond();

        });

        afterEach(function(){
            this.appc.close();
            this.server.restore();
        });

        it('"error" from TaskDescriptor caught by application', function(){

            expect(this.server).toBeDefined();
            expect(this.sources.models.length).toEqual(fixtures.tasks.data.length);

            var descriptor = this.sources.models[0].get('descriptor');

            spyOn(this.sources, 'remove');

            descriptor.save();
            // response for PUT /tasks/data/{id} returns 410
            this.server.respond();
            
            expect(this.sources.remove).toHaveBeenCalled();

        });

        it('Status 401 leads to refresh', function(){

            var descriptor = this.sources.models[0].get('descriptor');
            descriptor.save(null, {url: 'fakeurl'});

            spyOn(window.location, 'reload');
            // in real browser spy dont set
            if (window.location.reload.calls !== undefined) {
                this.server.respond('PUT', /fakeurl/, [401, {}, 'Session Expired']);
                expect(window.location.reload).toHaveBeenCalled();
            }
        });

    });
});
