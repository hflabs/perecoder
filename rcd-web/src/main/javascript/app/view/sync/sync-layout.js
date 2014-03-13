'use strict';

define([
    'underscore',
    'marionette',
    'view/view-helpers',
    'text!template/sync/sync.html'
], function (_, Marionette, ViewHelpers, viewTemplate) {

    /**
     * Sync page layout.
     * @class
     */
    var SyncLayout = Marionette.Layout.extend({
        template: _.template(viewTemplate),
        regions: {
            sourceRegion: '[data-region="tasks"]',
            popupsRegion: '[data-region="popups"]'
        },
        events: {
            'click [data-action="add"]': 'addSync'
        },
        templateHelpers: ViewHelpers.templateHelpers,
        addSync: function(e){
            var $button = $(e.target);
            if (!$button.is('[data-action="add"]')) {
                $button = $button.closest('[data-action="add"]');
            }
            var type = $button.attr('data-type');
            this.trigger('sync:add', type);
        }
    });

    return SyncLayout;

});
