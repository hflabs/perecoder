'use strict';

define(function(require){

    var _ = require('underscore'),
        $ = require('jasmine_jquery'),
        fixtures = require('fixtures'),
        MetafieldModel = require('model/content/metafield-model'),
        DictionaryPopupUploadView = require('view/dictionaries/dictionary-popup-upload-view'),
        ContentController = require('controller/content-controller');

    describe('Content Controller', function(){

        var DICTIONARY_ID = 'dictionary1';

        beforeEach(function(){

            // Create and setup server
            this.server = fixtures.createServer();

            // Create ans setup controller
            this.controller = new ContentController();

            // fetch models params
            this.server.respond();

            this.controller.open(fixtures.fakeRegion, [DICTIONARY_ID]);

            // fetch dictionary, group, metafields, records
            this.server.respond();

            this.server.requests = [];

        });

        afterEach(function(){
            this.controller.close();
            this.server.restore();
        });

        it('created, opened and configured', function(){

            expect(this.controller).toBeDefined();
            expect(this.controller.pageConfig).toBeDefined();
            expect(this.controller.recordsList).toBeTruthy();
            expect(this.controller.layout).toBeDefined();

            expect(MetafieldModel.prototype.modelParams.fields).toBeDefined();
        });

        it('loads columns and rows', function(){
            var dictionaryId = this.controller.currentDictionary.id;
            expect(this.controller.metafields.models.length)
                .toEqual(fixtures.metafields.dataOfDictionary(dictionaryId).length);
            expect(this.controller.recordsList.models.length)
                .toEqual(fixtures.records.dataOfDictionary(dictionaryId).content.length);
        });

        describe('has UI, that', function(){

            beforeEach(function(){
                this.layout = this.controller.layout;
                this.$table = this.layout.$('[data-control="records-main-table"]');
            });

            it('can show "Uplaod" popup', function(){
                var $btn = this.layout.$('[data-action="upload"]');
                expect($btn.length).toEqual(1);
                $btn.click();
                expect(this.layout.popupsRegion.currentView).toEqual(jasmine.any(DictionaryPopupUploadView));
            });

            it('can show "Add" row', function(){
                var $btn = this.$table.find('[data-action="add-record"]');
                expect($btn.length).toEqual(1);
                $btn.click();
                var $row = this.$table.find('tbody tr:first-child');
                expect($row).toHaveClass('editing');
                expect($row).toContain('input');
            });

            it('runs request on click "delete" button', function(){
                expect(this.$table.length).toEqual(1);
                var $btn = this.$table.find('i.delete').first();
                expect($btn.length).toEqual(1);
                $btn.click();

                var $confirmBtn = this.layout.$('.modal [data-action="submit"]');
                expect($confirmBtn.length).toEqual(1);
                $confirmBtn.click();

                expect(this.server.requests.length).toEqual(1);
                expect(this.server.requests[0].method).toEqual('DELETE');
                expect(this.server.requests[0].url).toMatch(fixtures.records.urls.delete);
            });

            it('can edit value', function(){
                var $btn = this.$table.find('[data-action="edit"]').first(),
                    $cell = $btn.closest('td');
                expect($btn.length).toEqual(1);
                $btn.click();

                var $editor = $cell.find('input');
                expect($editor).toExist();

                $editor.val(' newValue ').blur();
                expect(this.server.requests.length).toEqual(1);
                expect(this.server.requests[0].method).toEqual('PUT');
                expect(this.server.requests[0].url).toMatch(fixtures.records.urls.save);
                expect(this.server.requests[0].requestBody).toContain(':"newValue"');
            });

            it('can put search filter in request', function(){
                this.layout.$('.searchbar :text').val(' qwerty ASD ');
                this.layout.$('.searchbar i.search').click();
                expect(this.server.requests.length).toEqual(1);
                expect(this.server.requests[0].method).toEqual('GET');
                expect(this.server.requests[0].url).toContain('search=qwerty+ASD');
            });

            describe('has metafields editor', function(){

                beforeEach(function(){

                    // Switch to structure edit mode
                    this.$table.find('[data-action="structure-edit"]').click();

                    this.$header = this.$table.find('th');

                });

                it('with correct number of controls', function(){
                    expect(this.$header.find('input:text').length)
                        .toEqual(fixtures.metafields.dataOfDictionary(DICTIONARY_ID).length);

                    var nonPrimaryLength = _.filter(fixtures.metafields.dataOfDictionary(DICTIONARY_ID), function(metafield){
                        return !metafield.primary;
                    }).length;

                    expect(this.$header.find('i.unique').length)
                        .toEqual(nonPrimaryLength);
                    expect(this.$header.find('i.trash').length)
                        .toEqual(nonPrimaryLength);

                    expect(this.$header.find('[data-action="structure-add"]').length).toEqual(1);
                    expect(this.$header.find('[data-action="structure-ready"]').length).toEqual(1);
                });

                it('that can save metafield\'s name', function(){
                    var $editor = this.$header.find('input:text').first();
                    expect($editor).toExist();

                    $editor.val(' new name ').change();

                    expect(this.server.requests.length).toEqual(1);
                    expect(this.server.requests[0].method).toEqual('PUT');
                    expect(this.server.requests[0].url).toMatch(fixtures.metafields.urls.save);
                    expect(this.server.requests[0].requestBody).toContain('"name":"new name"');
                });

                it('that can save metafield\'s "unique" flag', function(){
                    var $editor = this.$header.find('i.unique').first();
                    expect($editor).toExist();

                    var wasUnique = !$editor.hasClass('not');
                    $editor.click();

                    expect(this.server.requests.length).toEqual(1);
                    expect(this.server.requests[0].method).toEqual('PUT');
                    expect(this.server.requests[0].url).toMatch(fixtures.metafields.urls.save);
                    expect(this.server.requests[0].requestBody).toContain('"unique":' + !wasUnique);
                });

                it('that sends "delete" request', function(){
                    var $editor = this.$header.find('[data-action="structure-delete"]').first();
                    expect($editor).toExist();

                    $editor.click();

                    var $confirmBtn = this.layout.$('.modal [data-action="submit"]');
                    expect($confirmBtn.length).toEqual(1);
                    $confirmBtn.click();

                    expect(this.server.requests.length).toEqual(1);
                    expect(this.server.requests[0].method).toEqual('DELETE');
                    expect(this.server.requests[0].url).toMatch(fixtures.metafields.urls.delete);
                });

                it('that sends "create" request', function(){
                    var $btn = this.$header.find('[data-action="structure-add"]').first();
                    expect($btn).toExist();

                    $btn.click();

                    expect(this.server.requests.length).toEqual(1);
                    expect(this.server.requests[0].method).toEqual('POST');
                    expect(this.server.requests[0].url).toMatch(fixtures.metafields.urls.create);
                });

            });

        });

    });

});
