'use strict';

define(function(require){

    var $ = require('jasmine_jquery'),
        i18n = require('locale/i18n'),
        fixtures = require('fixtures'),
        GroupsController = require('controller/groups-controller'),
        GroupPopupAddView = require('view/groups/group-popup-add-view');

    describe('Group create popup', function(){

        beforeEach(function(){
            // Create and setup server
            this.server = fixtures.createServer();

            // Create ans setup controller
            this.groupsController = new GroupsController();
            // fetch page config
            this.server.respond();

            this.groupsController.open(fixtures.fakeRegion);
            // fetch groups
            this.server.respond();

            this.groupsController.onGroupsAdd();
            this.view = this.groupsController.layout.popupsRegion.currentView;

            this.server.requests = [];

        });

        afterEach(function(){
            this.groupsController.close();
            this.server.restore();
        });

        it('opened and configured', function(){

            expect(this.view).toEqual(jasmine.any(GroupPopupAddView));
            expect(this.view.$el).toExist();

            var $name = this.view.$('[data-field="name"]'),
                $description = this.view.$('[data-field="description"]'),
                $saveBtn = this.view.$('[data-action="submit"]');

            expect($name.val()).toEqual('');
            expect($description.val()).toEqual('');
            expect($saveBtn).toExist();

            expect(+$name.attr('maxlength')).toEqual(fixtures.groups.models.fields.name.maxLength);
            expect(+$description.attr('maxlength')).toEqual(fixtures.groups.models.fields.description.maxLength);

        });

        it('shows error on saving empty values', function(){
            // set empty values
            this.view.$('[data-field]').val('').change();

            expect(this.server.requests.length).toEqual(0);
            var $errors = this.view.$('.help-block');
            expect($errors.length).toEqual(2);
            expect($errors.eq(0).text()).not.toEqual('');
            expect($errors.eq(1).text()).toEqual('');
        });

        it('saves correct group', function(){
            // set good name
            this.view.$('[data-field="name"]').val('qwerty').change();
            this.view.$('[data-field="description"]').val('abcd').change();
            this.view.$('[data-action="submit"]').click();

            expect(this.server.requests.length).toEqual(1);
            expect(this.server.requests[0].url).toMatch(fixtures.groups.urls.create);
            expect(this.server.requests[0].method).toEqual('POST');
            expect(this.server.requests[0].requestBody).toContain('"name":"qwerty"');

            this.server.respond();

            expect(this.view.$el).toBeHidden();
        });

    });

});