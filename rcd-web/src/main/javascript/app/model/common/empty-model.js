'use strict';

define(function(require) {
    var Backbone = require('backbone'),
        routes = require('routes');

    /**
     * Model for empty rows
     * @type {*}
     */
    var EmptyModel = Backbone.Model.extend({
    });

    return EmptyModel;
});