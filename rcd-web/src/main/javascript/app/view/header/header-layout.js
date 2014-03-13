'use strict';

define([
    'jquery',
    'underscore',
    'backbone',
    'marionette',
    'routes',
    'locale/i18n',
    'view/view-helpers',
    'text!template/header/header-layout.html'
], function ($, _, Backbone, Marionette, routes, i18n, ViewHelpers, viewTemplate) {

    /**
     * Application header widget.
     * @class
     */
    var HeaderLayout = Marionette.Layout.extend({

        template: _.template(viewTemplate),
        templateHelpers: ViewHelpers.templateHelpers,
        regions: {
            notificationsRegion: '[data-region="notifications"]',
            alertsRegion: '[data-region="alerts"]'
        },
        onBeforeRender: function() {
            this.model.set({ username: $('body').data('username') });
        },

        highlight: function (name) {
            var $menuItem = this.$('[data-name="' + name + '"]');
            this.$('li').removeClass('active');
            if ($menuItem) {
                $menuItem.addClass('active');
            }
        }

    });

    return HeaderLayout;

});
