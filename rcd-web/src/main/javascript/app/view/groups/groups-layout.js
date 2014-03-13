'use strict';

define(function(require){
    var _ = require('underscore'),
        i18n = require('locale/i18n'),
        Marionette = require('marionette'),
        ViewHelpers = require('view/view-helpers'),
        GroupsListView = require('view/groups/groups-list-view'),
        viewTemplate = require('text!template/groups/groups-layout.html');

    /**
     * Groups page layout.
     * Handles all the UI-only logic for groups.
     * @class
     */
    var GroupsLayout = Marionette.Layout.extend({
        template: _.template(viewTemplate),
        templateHelpers: ViewHelpers.templateHelpers,
        regions: {
            groupsRegion: '[data-region="groups"]',
            popupsRegion: '[data-region="popups"]'
        },
        events: {
            'keypress [data-field="filter"]': 'filterKeyup',
            'click span.search': 'applyFilter'
        },
        triggers: {
            'click [data-action="add"]': 'groups:add'
        },
        ui: {
            filter: '[data-field="filter"]'
        },
        initialize: function(options){
            this.groupsList = options.groupsList;
            this.groupsListView = new GroupsListView({ collection: this.groupsList });
        },
        onShow: function(){
            this.groupsRegion.show(this.groupsListView);
        },

        /* public */

        showPopup: function(popup) {
            this.popupsRegion.show(popup);
        },

        /* private */

        filterKeyup: function(e){
            if (e && (e.keyCode || e.which) == 13) {
                this.applyFilter();
            }
        },
        applyFilter: function(){
            this.trigger('groups:filter', this.ui.filter.val().trim());
        }
    });

    return GroupsLayout;

});
