'use strict';

define(function(require) {
    var Backbone = require('backbone'),
        Marionette = require('marionette'),
        routes = require('routes');

    /**
     * Application instance.
     * @class
     */
    var app = new Marionette.Application();

    /**
     * Application regions.
     */
    app.addRegions({
        header: '#header-wrapper',
        aside: '#right-wrapper',
        main: '#content-wrapper',
        footer: '#footer-wrapper'
    });

    /**
     * Listen for URL change.
     */
    app.on("initialize:after", function(){
        if (Backbone.history){
            Backbone.history.start({ pushState: true, root: routes.BASE_PATH });
        }
    });

    return app;
});
