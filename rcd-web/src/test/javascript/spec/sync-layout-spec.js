'use strict';

define(function(require){
    var $ = require('jasmine_jquery'),
        i18n = require('locale/i18n'),
        SyncController = require('controller/sync-controller'),
        fixtures = require('fixtures');

    require('bootstrap-switch');
    require('bootstrap-select');

    describe('SyncLayout', function(){

        beforeEach(function(){
            // Create server
            this.server = fixtures.createServer();

            this.syncController = new SyncController();
            // fetch model
            this.server.respond();

            this.syncController.open(fixtures.fakeRegion);
            this.layout = this.syncController.layout;

            // fetch sources
            this.server.respond();
            // fetch executing
            this.server.respond();

            this.server.requests = [];
        });

        afterEach(function(){
            this.server.restore();
            this.layout.$el.remove();
        });

        it('has all source views shown', function(){
            expect(this.layout.$('tbody').length).toEqual(fixtures.tasks.data.length);
        });

        it('click on Start button starts task', function(){
            var $a = this.layout.$('tbody:first [data-action="run-stop"]');
            expect($a).toExist();
            $a.click();
            expect(this.server.requests[0].url).toContain('/execute');
            // server execute a task
            this.server.respond();
            expect(this.server.requests[0].responseText).toContain('"status":"RUNNING"');
            expect(this.layout.$('tbody:first')).toContain('div.progress');
        });

        it('click on Cancel button cancels task', function(){
            var $a = this.layout.$('tbody:first [data-action="run-stop"]');
            expect($a).toExist();
            $a.click();
            expect(this.server.requests[0].url).toContain('/cancel');
            // server cancel task
            this.server.respond();
            expect(this.server.requests[0].responseText).toContain('INTERRUPTING');
            expect(this.layout.$('tbody:first')).toContain('div.progress');
            expect(this.layout.$('tbody:first button')).toHaveAttr('disabled');
        });

        it('click on Trash button deletes task', function(){
            var $a = this.layout.$('tbody [data-action="delete"]').first();
            expect($a).toExist();
            $a.click();

            var $submitBtn = this.layout.$('.modal [data-action="submit"]');
            expect($submitBtn.length).toEqual(1);
            $submitBtn.click();

            expect(this.server.requests[0].method).toEqual('DELETE');
        });

        it('opens popup when click on "settings" label', function(){
            var $a = this.layout.$('tbody:first [data-action="settings"]');
            expect($a).toExist();
            $a.click();
            expect(this.layout.$('[data-region="popups"] .modal:visible')).toExist();

            var $form = this.layout.$('.modal:visible');
            expect($form).toExist();

            // reset all values to empty
            $form.find('[data-field="parameters.url"]').val('');
            $form.find('[data-field="parameters.timeout"]').val('');

            // reset parameters attribute of model
            this.syncController.sourceList.models[0].get('descriptor').set('parameters', {});

            // click Save button to validate form
            $form.find('[data-action="save"]').click();
            var emptyText = i18n.t('model.validator.required');
            expect($form.find('[data-field="parameters.url"]').closest('.control-group').find('.help-block')).toHaveText(emptyText);
            expect($form.find('[data-field="parameters.timeout"]').closest('.control-group').find('.help-block')).toHaveText(emptyText);
        });

        it('opens popup when click on cron label', function(){
            var $a = this.layout.$('tbody:first [data-action="edit"]');
            expect($a).toExist();
            $a.click();
            expect(this.layout.$('.modal:visible')).toExist();
        });

    });

});