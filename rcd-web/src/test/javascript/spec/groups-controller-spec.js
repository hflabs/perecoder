'use strict';

define(function(require){

    var _ = require('underscore'),
        $ = require('jasmine_jquery'),
        i18n = require('locale/i18n'),
        fixtures = require('fixtures'),
        GroupModel = require('model/groups/group-model'),
        GroupsController = require('controller/groups-controller'),
        GroupPopupAddView = require('view/groups/group-popup-add-view');

    describe('GroupsController', function(){
        
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

            this.server.requests = [];

        });

        afterEach(function(){
            this.groupsController.close();
            this.server.restore();
        });

        it('created, opened and configured', function(){
            expect(this.groupsController).toBeDefined();
            expect(this.groupsController.groupsList).toBeDefined();
            expect(this.groupsController.layout).toBeDefined();

            expect(GroupModel.prototype.modelParams.fields).toBeDefined();
        });

        it('load groups', function(){
            this.server.respond();
            expect(this.groupsController.groupsList.models.length).toEqual(fixtures.groups.data.length);
        });

        it('shows add popup', function(){
            var $addBtn = this.groupsController.layout.$('[data-action="add"]');
            expect($addBtn.length).toEqual(1);
            $addBtn.click();
            expect(this.groupsController.layout.popupsRegion.currentView).toEqual(jasmine.any(GroupPopupAddView));

        });

    });
    
});
