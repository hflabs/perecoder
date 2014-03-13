'use strict';

define([
    'jasmine_jquery',
    'common/local-date'
], function($, LocalDate) {

    describe('LocalDate', function(){
        
        var date = new LocalDate(new Date(2012, 11, 15, 10, 0, 2).valueOf());
        
        it('created', function(){
            expect(date).toBeDefined();
        });
        
        it('makes correct date string', function(){
            expect(date.toDateString()).toEqual('15.12.2012');
        });

        it('makes correct time string', function(){
            expect(date.toTimeString()).toEqual('10:00');
        });
    });

});