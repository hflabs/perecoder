'use strict';

define(function(require){

    var _ = require('underscore'),
        $ = require('jasmine_jquery'),
        i18n = require('locale/i18n'),
        GroupModel = require('model/groups/group-model'),
        GroupsList = require('model/groups/groups-list'),
        GroupsLayout = require('view/groups/groups-layout'),
        fixtures = require('fixtures');

    describe('GroupsLayout', function(){

        beforeEach(function(){
            this.groupsList = new GroupsList(fixtures.groups.data);
            this.groupsLayout = new GroupsLayout({groupsList: this.groupsList});

            fixtures.fakeRegion.show(this.groupsLayout);

            var requests = this.requests = [];
            this.xhr = sinon.useFakeXMLHttpRequest();
            this.xhr.onCreate = function(xhr){
                requests.push(xhr);
            }

            GroupModel.prototype.modelParams = _.clone(fixtures.groups.models);
        });

        afterEach(function(){
            this.xhr.restore();
            this.groupsLayout.close();
            this.groupsLayout.$el.remove();
        });

        it('created', function(){

            expect(this.groupsList.models.length).toEqual(fixtures.groups.data.length);
            expect(this.groupsLayout).toBeDefined();
            expect(this.groupsLayout.$el).toExist();
            expect(this.groupsLayout.groupsListView).toBeDefined();
            expect(this.groupsLayout.groupsListView.$el).toExist();
            expect(this.groupsLayout.groupsListView.$('article').length).toEqual(fixtures.groups.data.length);

        });

        it('read-only groups do not have "delete" and "edit" buttons', function(){
            var writableCount = _.where(fixtures.groups.data, {writable:true}).length;
            expect(this.groupsLayout.groupsListView.$('article i.trash').length)
                .toEqual(writableCount);
            expect(this.groupsLayout.groupsListView.$('article i.edit').length)
                .toEqual(writableCount);
        });

        it('put filter in request', function(){
            spyOn(this.groupsLayout, 'trigger');
            var phrase = ' qwerty ';
            this.groupsLayout.$('.searchbar :text').val(phrase);
            this.groupsLayout.$('.searchbar span.search').click();
            expect(this.groupsLayout.trigger).toHaveBeenCalledWith('groups:filter','qwerty');
        });

        it('can edit name', function(){
            var $views = this.groupsLayout.groupsListView.$('.tile:has(.edit)'),
                $view = $views.first();
            expect($view.length).toBeGreaterThan(0);

            var $editBtn = $view.find('i.edit');
            expect($editBtn.length).toEqual(1);
            $editBtn.mousedown();

            var $editor = $view.find('input');
            expect($editor).toExist();
            expect(+$editor.attr('maxlength')).toEqual(fixtures.groups.models.fields.name.maxLength);

            $editor.val(' newname ').blur();
            expect(this.requests.length).toEqual(1);
            expect(this.requests[0].method).toEqual('PUT');
            expect(this.requests[0].url).toMatch(/groups\/data\/group1id/);
            expect(this.requests[0].requestBody).toContain('"name":"newname"');
        });

        it('can edit description', function(){
            var $views = this.groupsLayout.groupsListView.$('.tile:has(.edit)'),
                $view = $views.first();
            expect($view.length).toBeGreaterThan(0);

            var $editBtn = $view.find('[data-mode="edit-description"]');
            expect($editBtn.length).toEqual(1);
            $editBtn.mousedown();

            var $editor = $view.find('textarea');
            expect($editor).toExist();

            $editor.val(' newdesc ').blur();
            expect(this.requests.length).toEqual(1);
            expect(this.requests[0].method).toEqual('PUT');
            expect(this.requests[0].url).toMatch(/groups\/data\/group1id/);
            expect(this.requests[0].requestBody).toContain('"description":"newdesc"');
        });

        it('can delete group', function(){
            var $views = this.groupsLayout.groupsListView.$('.tile:has(.edit)'),
                $view = $views.first();
            expect($view.length).toBeGreaterThan(0);

            var $editBtn = $view.find('.trash');
            expect($editBtn.length).toEqual(1);
            $editBtn.mousedown();

            expect($view).toContain('.buttons button');

            var $yesBtn = $view.find('[data-action="destroy"]');
            $yesBtn.click();
            expect(this.requests.length).toEqual(1);
            expect(this.requests[0].method).toEqual('DELETE');
            expect(this.requests[0].url).toMatch(/groups\/data\/group1/);
        });

    });

});
