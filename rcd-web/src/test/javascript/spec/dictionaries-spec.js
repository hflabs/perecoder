'use strict';

define(function(require){

    var _ = require('underscore'),
        $ = require('jasmine_jquery'),
        fixtures = require('fixtures'),
        DictionaryModel = require('model/dictionaries/dictionary-model'),
        DictionariesController = require('controller/dictionaries-controller'),
        DictionaryPopupAddView = require('view/dictionaries/dictionary-popup-add-view'),
        DictionaryPopupUploadView = require('view/dictionaries/dictionary-popup-upload-view');

    describe('Dictionaries Controller', function(){

        beforeEach(function(){

            // Create server
            this.server = fixtures.createServer();

            // Create ans setup controller
            this.controller = new DictionariesController();
            // fetch models params
            this.server.respond();

            this.controller.open(fixtures.fakeRegion, []);
            // fetch groups
            this.server.respond();
            // fetch dictionaries
            this.server.respond();

            this.server.requests = [];

        });

        afterEach(function(){
            this.controller.close();
            this.server.restore();
        });

        it('created, opened and configured', function(){
            expect(this.controller).toBeDefined();
            expect(this.controller.groupsList).toBeDefined();
            expect(this.controller.list).toBeDefined();
            expect(this.controller.layout).toBeDefined();

            expect(DictionaryModel.prototype.modelParams.fields).toBeDefined();
        });

        it('loads groups and dictionaries', function(){
            expect(this.controller.groupsList.models.length)
                .toEqual(fixtures.groups.data.length);
            expect(this.controller.list.models.length)
                .toEqual(fixtures.dictionaries.data.content.length);
        });

        describe('has UI, that', function(){

            beforeEach(function(){
                this.layout = this.controller.layout;
                this.$table = this.layout.$('[data-control="dictionaries-main-table"]');
            });

            it('can show "Add" popup', function(){
                var $btn = this.layout.$('[data-action="add"]');
                expect($btn.length).toEqual(1);
                $btn.click();
                expect(this.layout.popupsRegion.currentView).toEqual(jasmine.any(DictionaryPopupAddView));
            });

            it('can show "Uplaod" popup', function(){
                var $btn = this.layout.$('[data-action="upload"]');
                expect($btn.length).toEqual(1);
                $btn.click();
                expect(this.layout.popupsRegion.currentView).toEqual(jasmine.any(DictionaryPopupUploadView));
            });

            it('shows "delete" button only for writable dictionaries', function(){
                var writableCount = _.where(fixtures.dictionaries.data.content, {writable:true}).length;
                expect(this.$table.find('.delete-cell i.delete').length)
                    .toEqual(writableCount);
            });

            it('runs request on click "delete" button', function(){
                var $btn = this.$table.find('i.delete').first();
                expect($btn.length).toEqual(1);
                $btn.click();

                var $confirmBtn = this.layout.$('.modal [data-action="submit"]');
                expect($confirmBtn.length).toEqual(1);
                $confirmBtn.click();

                expect(this.server.requests.length).toEqual(1);
                expect(this.server.requests[0].method).toEqual('DELETE');
                expect(this.server.requests[0].url).toMatch(fixtures.dictionaries.urls.delete);
            });

            it('can edit name', function(){
                var $btn = this.$table.find('i.edit').first(),
                    $cell = $btn.closest('td');
                expect($btn.length).toEqual(1);
                $btn.click();

                var $editor = $cell.find('input');
                expect($editor).toExist();
                expect(+$editor.attr('maxlength')).toEqual(DictionaryModel.prototype.modelParams.fields.name.maxLength);

                $editor.val('newname').blur();
                expect(this.server.requests.length).toEqual(1);
                expect(this.server.requests[0].method).toEqual('PUT');
                expect(this.server.requests[0].url).toMatch(fixtures.dictionaries.urls.save);
                expect(this.server.requests[0].requestBody).toContain('"name":"newname"');
            });

            it('can edit description', function(){
                var $btn = this.$table.find('[data-action="edit"]').first(),
                    $cell = $btn.closest('td');
                expect($btn.length).toEqual(1);
                $btn.click();

                var $editor = $cell.find('input');
                expect($editor).toExist();
                expect(+$editor.attr('maxlength')).toEqual(DictionaryModel.prototype.modelParams.fields.description.maxLength);

                $editor.val('newdesc').blur();
                expect(this.server.requests.length).toEqual(1);
                expect(this.server.requests[0].method).toEqual('PUT');
                expect(this.server.requests[0].url).toMatch(fixtures.dictionaries.urls.save);
                expect(this.server.requests[0].requestBody).toContain('"description":"newdesc"');
            });

            it('can put search filter in request', function(){
                this.layout.$('.searchbar :text').val(' qwerty ASD ');
                this.layout.$('.searchbar span.search').click();
                expect(this.server.requests.length).toEqual(1);
                expect(this.server.requests[0].method).toEqual('GET');
                expect(this.server.requests[0].url).toContain('search=qwerty+ASD');
            });

            it('can forward to content page', function(){
                spyOn(this.controller, 'trigger');
                this.$table.find('tbody tr').first().click();
                expect(this.controller.trigger).toHaveBeenCalledWith('navigate', '/rcd/admin/records/dictionary1/');
            });

        });

    });

});
