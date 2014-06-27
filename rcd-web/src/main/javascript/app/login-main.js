'use strict';

/**
 * RequireJS configuration.
 */
require.config({
    shim: {
        'underscore': {
            exports: '_'
        }
    },
    paths: {
        'jed': '../lib/jed',
        'jquery': '../../webjars/jquery/1.8.3/jquery',
        'log': '../lib/loglevel',
        'text': '../lib/text',
        'underscore': '../../webjars/underscorejs/1.5.2/underscore'
    },
    urlArgs: "_=" + new Date().valueOf()
});

/**
 * Application bootstrap.
 */
require([
    'underscore',
    'jquery',
    'locale/i18n',
    'text!template/common/login.html'
], function (_, $, i18n, viewTemplate) {

    $(function() {
        window.title = i18n.tc('view.login.title');

        var $body = $('body'),
            template = _.template(viewTemplate);

        $body.attr('data-locale', i18n.options.locale_data.messages[''].lang);
        $('#page-wrapper').html(template({
            formAction: $body.attr('data-action'),
            errorMessage: $body.attr('data-error-message') || '',
            t: i18n.t,
            tc: i18n.tc
        }));
    });

});
