'use strict';

define(function(require) {
    var _ = require('underscore'),
        Jed = require('jed'),
        en = require('locale/en'),
        ru = require('locale/ru'),
        util = require('util'),
        log = require('log');

    var Languages = {
        DEFAULT_LANGUAGE: ru,
        LANGUAGES: [en, ru],

        getLanguageNames: function () {
            return _.map(this.LANGUAGES, function (language) {
                return language.messages[''].lang;
            })
        },

        findLanguageByName: function (name) {
            var index = _.indexOf(this.getLanguageNames(), name);
            if (index === -1) {
                return this.DEFAULT_LANGUAGE;
            } else {
                return this.LANGUAGES[index];
            }
        },

        guessUserLanguage: function () {
            var languageName = navigator.language || navigator.userLanguage;
            languageName = languageName.split('-').shift();
            return this.findLanguageByName(languageName);
        }
    };

    var i18n = new Jed({

        missing_key_callback: function (key) {
            log.error('Missing localized string:' + key);
        },

        locale_data: Languages.guessUserLanguage()
    });

    i18n.capitalize = util.capitalize;

    i18n.setLanguage = function (name) {
        this.options.locale_data = Languages.findLanguageByName(name);
    };

    i18n.t = function (text, count, options){
        return i18n.translate(text).ifPlural(count).fetch(options || count);
    };

    i18n.tc = function (text, count, options){
        return util.capitalize(this.t.apply(this, arguments));
    };

    return i18n;
});
