'use strict';

define(function(require) {
    var _ = require('underscore'),
        i18n = require('locale/i18n'),
        SyncSource = require('model/sync/sync-source'),
        TaskDescriptor = require('model/sync/task-descriptor'),
        fixtures = require('fixtures');

    TaskDescriptor.prototype.TASK_TYPES = _.indexBy(fixtures.tasks.models, 'id');

    describe('SyncSource model', function(){
        
        beforeEach(function(){
            this.source = new SyncSource(_.clone(fixtures.tasks.data[0]), {parse: true});
            this.descriptor = this.source.get('descriptor');
        });
        
        it('created successfully', function(){
            expect(this.source).toBeDefined();
        })

        it('descriptor is an instance of TaskDescriptor', function(){
            expect(this.descriptor).toBeDefined();
            expect(this.descriptor).toEqual(jasmine.any(TaskDescriptor));
        });

        it('doesn\'t replace descriptor (but update it) when new data is set', function(){
            this.source.set(this.source.parse({
                descriptor: {
                    name: 'dummyTaskPerformer'
                },
                result: null
            }));
            expect(this.descriptor).toBe( this.source.get('descriptor') );
            expect(this.descriptor).toEqual(jasmine.any(TaskDescriptor));
            expect(this.descriptor.get('name')).toEqual('dummyTaskPerformer');
        });
        
    });
    
});