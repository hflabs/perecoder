'use strict';

define(function(require){

    var _ = require('underscore'),
        $ = require('jasmine_jquery'),
        fixtures = require('fixtures'),
        RecodesAsideController = require('controller/recodes-aside-controller');

    describe('Recodes Right-side Controller', function(){

        var RRS_ID = 'rrs1';

        beforeEach(function(){

            // Create and setup server
            this.server = fixtures.createServer();

            this.controller = new RecodesAsideController();

            this.controller.open(fixtures.fakeRegion, [RRS_ID]);

            // fetch rrs, targets, metafields, records
            this.server.respond();

            this.server.requests = [];

        });

        afterEach(function(){
            this.controller.close();
            this.server.restore();
        });

        it('created, opened and configured', function(){
            expect(this.controller).toBeDefined();
            expect(this.controller.targets).toBeTruthy();
            expect(this.controller.recodes).toBeTruthy();
            expect(this.controller.records).toBeTruthy();
            expect(this.controller.metafields).toBeTruthy();
            expect(this.controller.layout).toBeDefined();
        });

        it('loads targets, columns', function(){
            var dictionaryId = this.controller.toDictionary.id;
            expect(this.controller.targets.models.length)
                .toEqual(fixtures.rrs.dataOfDictionary(dictionaryId,'FROM').length);
            expect(this.controller.metafields.models.length)
                .toEqual(fixtures.metafields.dataOfDictionary(dictionaryId).length);
        });

        describe('after receiving source records\' ids', function(){

            var SOURCE_RECORD_IDS = ['record1','record2'];

            beforeEach(function(){
                this.controller.onRecodeFrom(SOURCE_RECORD_IDS);

                // fetch targets, recodes, metafields, records
                this.server.respond();

                this.server.requests = [];
            });

            it('has recodes and destination records loaded', function(){
                expect(this.controller.recodes.models.length)
                    .toEqual(fixtures.recodes.dataOfRRS(RRS_ID, SOURCE_RECORD_IDS).length);
                expect(this.controller.records.models.length)
                    .toBeGreaterThan(0);
            });

        });

    });

});
