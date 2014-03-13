'use strict';

define(function(require){

    var _ = require('underscore'),
        $ = require('jasmine_jquery'),
        fixtures = require('fixtures'),
        RecodesController = require('controller/recodes-controller');

    describe('Recodes Left-side Controller', function(){

        var RRS_ID = 'rrs1';

        beforeEach(function(){

            // Create and setup server
            this.server = fixtures.createServer();

            this.controller = new RecodesController();

            this.controller.open(fixtures.fakeRegion, [RRS_ID]);

            // fetch rrs, records, metafields
            this.server.respond();

            this.server.requests = [];

        });

        afterEach(function(){
            this.controller.close();
            this.server.restore();
        });

        it('created, opened and configured', function(){
            expect(this.controller).toBeDefined();
            expect(this.controller.records).toBeTruthy();
            expect(this.controller.metafields).toBeTruthy();
            expect(this.controller.layout).toBeDefined();
        });

        it('loads columns and rows', function(){
            var dictionaryId = this.controller.fromDictionary.id;
            expect(dictionaryId).toEqual('dictionary1');
            expect(this.controller.metafields.models.length)
                .toEqual(fixtures.metafields.dataOfDictionary(dictionaryId).length);
            expect(this.controller.records.models.length)
                .toEqual(fixtures.records.dataOfDictionary(dictionaryId).content.length);
        });

        describe('has UI, that', function(){

            beforeEach(function(){
                this.layout = this.controller.layout;
                this.$table = this.layout.$('[data-control="recodes-from-table"]');
            });

            it('can put search filter in request', function(){
                this.layout.$('.searchbar :text').val(' qwerty ASD ');
                this.layout.$('.searchbar i.search').click();
                expect(this.server.requests.length).toEqual(1);
                expect(this.server.requests[0].method).toEqual('GET');
                expect(this.server.requests[0].url).toContain('search=qwerty+ASD');
            });

            it('can sort rows', function(){
                var $btn = this.$table.find('th .sortable').first();
                expect($btn).toExist();

                $btn.click();

                expect(this.server.requests.length).toEqual(1);
                expect(this.server.requests[0].method).toEqual('GET');
                expect(this.server.requests[0].url).toMatch(fixtures.records.urls.fetchAll);
                expect(this.server.requests[0].url).toContain('&sortOrderKey=metafield');
            });

            it('can rows checking', function(){
                var $check = this.$table.find('tbody :checkbox').first();
                expect($check.length).toEqual(1);

                $check.click();

                expect(this.layout.$('[data-field="selection"]')).toBeVisible();
            });

            it('puts pins above rows', function(){
                this.controller.onRecodesUnmatched(['record1']);
                expect(this.$table.find('tr.pinned').length).toEqual(1);

                this.controller.onRecodesUnmatched(['record1', 'record2']);
                expect(this.$table.find('tr.pinned').length).toEqual(2);
            });

        });

    });

});
