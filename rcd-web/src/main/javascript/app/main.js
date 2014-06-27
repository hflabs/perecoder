'use strict';

/**
 * RequireJS configuration.
 */
require.config({
    shim: {
        'underscore': {
            exports: '_'
        },
        'backbone': {
            deps: [
                'underscore',
                'jquery'
            ],
            exports: 'Backbone'
        },
        'bootstrap': ['jquery'],
        'bootstrap-fileupload': ['jquery'],
        'bootstrap-switch': ['jquery'],
        'bootstrap-select': ['jquery'],
        'backgrid': {
            deps: [
                {},
                'jquery',
                'underscore',
                'backbone'
            ],
            exports: 'Backgrid'
        },
        'backgrid-select-all': {
            deps: [
                'jquery',
                'underscore',
                'backbone',
                'backgrid',
            ],
            exports: 'Backgrid'
        },
        'jquery-form': ['jquery'],
        'jquery.fileDownload': ['jquery', window]
    },
    paths: {
        'backbone': '../../webjars/backbonejs/1.0.0/backbone',
        'backbone.wreqr': '../lib/backbone.wreqr',
        'backbone.babysitter': '../lib/backbone.babysitter',
        'backbone-pageable': '../lib/backbone-pageable',
        'backgrid': '../lib/backgrid',
        'backgrid-select-all': '../lib/backgrid-select-all',
        'bootstrap': '../../webjars/bootstrap/2.3.1/js/bootstrap',
        'bootstrap-fileupload': '../lib/bootstrap-fileupload',
        'bootstrap-switch': '../lib/bootstrap-switch',
        'bootstrap-select': '../lib/bootstrap-select',
        'jed': '../lib/jed',
        'jquery': '../../webjars/jquery/1.8.3/jquery',
        'jquery.fileDownload': '../lib/jquery.fileDownload',
        'jquery-form': '../lib/jquery.form',
        'log': '../lib/loglevel',
        'marionette' : '../lib/backbone.marionette',
        'text': '../lib/text',
        'underscore': '../../webjars/underscorejs/1.5.2/underscore'
    },
    urlArgs: "_=" + new Date().valueOf(),
    catchError: true
});

/**
 * Scritp error handling
 * @param err
 */
require.onError = function (err) {
    var errorMessage = 'Error loading javascript modules: ' + err.requireType + ' (' + err.requireModules + ')';
    console.log(errorMessage);
    var errorHtml = '<div class="alert alert-error" style="top: 0;"><span>'
            + errorMessage + '</span></div>';
    document.body.className = 'error';
    document.getElementById('content-wrapper').innerHTML = errorHtml;
};

/**
 * Load plugins and extensions
 */
require([
    'jquery-form',
    'jquery.fileDownload',
    'bootstrap',
    'bootstrap-fileupload',
    'bootstrap-switch',
    'bootstrap-select'
], function(){});

/**
 * Application bootstrap.
 */
require([
    'app',
    'controller',
    'router'
], function (app, AppController, AppRouter) {

    app.addInitializer(function(){
        app.controller = new AppController();
        app.router = new AppRouter({
            controller: app.controller
        });
        app.router.initAppRoutes();
        app.controller.start();
    });

    $(function() {
        app.start();
    });

});
