'use strict';

define(function(require) {
    var Backbone = require('backbone'),
        routes = require('routes');

    /**
     * Application version data model.
     * @type {*}
     */
    var Version = Backbone.Model.extend({
        url: routes.absoluteUrl(routes.VERSION)
    });

    return Version;
});