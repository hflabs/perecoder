'use strict';

define(function (require) {
    var routes = require('routes'),
        DictionaryModel = require('model/dictionaries/dictionary-model'),
        GridCollection = require('model/grid-collection');

    var DictionariesList = GridCollection.extend({
        model: DictionaryModel,
        url: routes.absoluteUrl(routes.DICTIONARIES_DATA)
    });

    return DictionariesList;
});

