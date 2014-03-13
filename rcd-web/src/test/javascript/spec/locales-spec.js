'use strict';

define([
    'underscore',
    'locale/ru',
    'locale/en'
], function(_, ru, en) {

    describe('I18n locales', function(){

        it('have same message keys', function(){
            var ruKeys = _.keys(ru.messages),
                enKeys = _.keys(en.messages);

            ruKeys.sort();
            enKeys.sort();

            expect(_.difference(ruKeys, enKeys)).toEqual([]);
            expect(_.difference(enKeys, ruKeys)).toEqual([]);
        });

    });

});