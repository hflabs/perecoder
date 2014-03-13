'use strict';

define(function(require) {
    var _ = require('underscore'),
        i18n = require('locale/i18n'),
        Cron = require('common/cron-util'),
        LocalDate = require('common/local-date'),
        TaskDescriptor = require('model/sync/task-descriptor'),
        fixtures = require('fixtures');

    describe('TaskDescriptor model', function(){
    
        TaskDescriptor.prototype.TASK_TYPES = _.indexBy(fixtures.tasks.models, 'id');

        describe('Parameters validation', function(){
        
            describe('CNSI', function(){
        
                var descriptor = new TaskDescriptor(fixtures.tasks.descriptorCNSI, { parse: true });

                it('Model parsed', function(){
                    expect(descriptor.get('cron')).toEqual(jasmine.any(Cron));
                    expect(descriptor.get('nextScheduledDate')).toEqual(jasmine.any(LocalDate));
                });
        
                it('Setting empty values', function(){
                    var result = descriptor.set({
                            parameters: {
                                url: '',
                                timeout: ''
                            }
                        }, {validate: true});
                    
                    expect(result).toEqual(false);
                    expect(descriptor.validationError).toEqual({
                        fieldErrors: {
                            'parameters.url': i18n.t('model.validator.required'),
                            'parameters.timeout': i18n.t('model.validator.required')
                        }
                    });
                });
                
                it('Setting not-numeric values', function(){
                    var result = descriptor.set({
                            parameters: {
                                url: 'http://localhost',
                                timeout: 'a10'
                            }
                        }, {validate: true});
                    
                    expect(result).toEqual(false);
                    expect(descriptor.validationError).toEqual({
                        fieldErrors: {
                            'parameters.timeout': i18n.t('model.validator.type')
                        }
                    });
                });

            });
            
            describe('dummy', function(){
            
                var descriptor = new TaskDescriptor(fixtures.tasks.descriptorDummy, { parse: true });

                it('Setting not-numeric values', function(){
                    var result = descriptor.set({
                            parameters: {
                                count: NaN,
                                delay: new Date()
                            }
                        }, {validate: true});
                    
                    expect(result).toEqual(false);
                    expect(descriptor.validationError).toEqual({
                        fieldErrors: {
                            'parameters.count': i18n.t('model.validator.type'),
                            'parameters.delay': i18n.t('model.validator.type')
                        }
                    });
                });

            });
        });
        
        describe('Server operations', function(){
        
            beforeEach(function(){
                this.descriptor = new TaskDescriptor(fixtures.tasks.descriptorCNSI, { parse: true });
            
                this.server = sinon.fakeServer.create();
                this.server.respondWith('GET',/^\/rcd\/admin\/tasks\/data\//, [200, {'Content-Type': 'application/json'},
                    JSON.stringify(fixtures.tasks.descriptorCNSI)
                ]);
                this.server.respondWith('PUT',/^\/rcd\/admin\/tasks\/data\//, [200, {'Content-Type': 'application/json'},
                    JSON.stringify(_.extend({},fixtures.tasks.descriptorCNSI,{status:'RUNNING'}))
                ]);
            });
            
            afterEach(function(){
                this.server.restore();
            });
            
            it('Fetches correctly', function(){
                this.descriptor.set({ description: '' });
                this.descriptor.fetch();
                this.server.respond();
                expect(this.descriptor.get('description')).toEqual(fixtures.tasks.descriptorCNSI.description);
            });
            
            it('Can be executed', function(){

                this.descriptor.save();
                this.server.respond();
                expect(this.descriptor.get('status')).toEqual('RUNNING');

            });
            
        });

    });

});
