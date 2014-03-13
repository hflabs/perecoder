'use strict';

define(function(require) {
    var _ = require('underscore');

    var crudMethods = ['create','read','update','delete','patch'];
    var PRELOADER_DELAY = 5000;

    var AppSync = function(bbsync, vent) {

        return function(method,model,options){

            function disableControls(disabled){
                if (options.disableControls) {
                    options.disableControls.attr('disabled', !!disabled);
                }
            }

            options || (options = {});
            options.cache = false;

            disableControls(true);

            var xhr;
            if (crudMethods.indexOf(method) >=0) {
                xhr = bbsync(method, model, options);
            } else {
                var url = options.url;
                if (!url) {
                    url = _.result(model, 'url');
                    if (!/\/$/.test(url)) {
                        url += '/';
                    }
                    if (method) {
                        url += method;
                    }
                }
                xhr = bbsync(null, model, _.extend({
                    url: url,
                    type: 'PUT'
                },options))
                    .done(function(resp){
                        if (method) {
                            model.trigger('sync', model, resp, options);
                            model.trigger('sync:' + method, model, resp, options);
                        }
                    });
            }
            if (xhr) {
                var requestTimer = setTimeout(function(){
                    vent && vent.trigger('hang', xhr);
                }, PRELOADER_DELAY);
                xhr.fail(function(jqXHR){
                    vent && vent.trigger('error', model, jqXHR, options);
                })
                    .always(function(){
                        clearTimeout(requestTimer);
                        disableControls(false);
                    });
            } else {
                disableControls(false);
            }
            return xhr;
        };

    };

    return AppSync;
});