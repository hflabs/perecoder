'use strict';

define(function(require) {
    var Backbone = require('backbone'),
        routes = require('routes');

    /**
     * Application header data model.
     * @type {*}
     */
    var Header = Backbone.Model.extend({
        defaults: {
            navigation: [
                {
                    name: 'groups',
                    url: routes.GROUPS,
                    active: false
                },
                {
                    name: 'dictionaries',
                    url: routes.DICTIONARIES,
                    active: false
                },
                {
                    name: 'tasks',
                    url: routes.TASKS,
                    active: false
                }
            ]
        }
    });

    return Header;

});
