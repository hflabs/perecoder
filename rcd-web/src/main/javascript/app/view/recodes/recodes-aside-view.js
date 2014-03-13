'use strict';

define(function (require) {
    var _ = require('underscore'),
        $ = require('jquery'),
        Marionette = require('marionette'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        RRSTargetsView = require('view/recodes/rrs-targets-view'),
        RecordSelectorView = require('view/recodes/record-selector-view'),
        ViewHelpers = require('view/view-helpers'),
        viewTemplate = require('text!template/recodes/recodes-aside-layout.html');

    var windowEventNS = '.aside_recodes';

    /**
     * Recode rules layout.
     * Handles all the UI-only logic for recodes.
     * @class
     */
    var RecodesAsideLayout = Marionette.Layout.extend({
        template: _.template(viewTemplate),
        regions: {
            targetsRegion: '[data-region="targets"]',
            recordsRegion: '[data-region="record-selector"]'
        },
        ui: {
            target: '.recode-target',
            spacer: '.spacer'
        },
        templateHelpers: ViewHelpers.templateHelpers,
        events: {
            'click .recode-target': 'onTargetClick'
        },
        triggers: {
            'click [data-action="done"]': 'aside:done',
            'click [data-action="new"]': 'aside:new',
            'click [data-action="delete"]': 'rrs:delete',
            'click [data-action="settings"]': 'rrs:settings'
        },
        initialize: function (options) {
            this.metafields = options.metafields;
            this.targets = options.targets;
            this.records = options.records;
            this.dropdownRecords = options.dropdownRecords;
            this.createParts();
            this.listenTo(this.model, 'change', this.onRRSChange);
        },
        onShow: function () {
            this.targetsRegion.show(this.targetsView);
            this.recordsRegion.show(this.recordSelectorView);
            this.bindDropdownEvents();
        },
        onClose: function () {
            this.unbindDropdownEvents();
        },

        /* public */

        /**
         * Updates spacers height to make tables to be on the same vertical position
         * @param $spacer
         */
        resizeSpacers: function($spacer){
            if (!$spacer || !$spacer.length) {
                return;
            }
            var $localSpacer = this.ui.spacer;
            var relativeTop = $localSpacer.offset().top - $spacer.offset().top;
            if (relativeTop < 0) {
                $localSpacer.height(-relativeTop);
                $spacer.height(0);
            } else if (relativeTop > 0) {
                $localSpacer.height(0);
                $spacer.height(relativeTop);
            } else {
                $localSpacer.height(0);
                $spacer.height(0);
            }
        },

        resizeColumns: function(){
            this.recordSelectorView.resizeColumns();
        },

        /* private */

        createParts: function () {
            this.targetsView = new RRSTargetsView({ collection: this.targets });

            this.recordSelectorView = new RecordSelectorView({
                metafields: this.metafields,
                records: this.records,
                dropdownRecords: this.dropdownRecords,
                resetText: i18n.tc('view.recodes.reset'),
                selectedMetafieldId: this.model.get('toMetaFieldId')
            });
        },

        onTargetClick: function(e){
            e.stopPropagation();
            this.targetsRegion.$el.toggle();
        },

        bindDropdownEvents: function(){
            var dropdownClicked = false,
                $list = this.targetsRegion.$el,
                handler = function(){
                    if ($list.length && $list.is(':visible') && !dropdownClicked) {
                        $list.hide();
                    }
                    dropdownClicked = false;
                };

            $('body').on('click'+windowEventNS, handler)
                .on('keydown', function(e){
                    if (e.keyCode == 27) {
                        handler();
                    }
                });

            $list.on('click'+windowEventNS, function(){
                dropdownClicked = true;
            });
        },

        unbindDropdownEvents: function(){
            $('body').off(windowEventNS);
        },

        /**
         * @param fromId opens at row, which's 'fromRecordId' attribute equals to this
         */
        showDropdown: function(fromIds){
            var model = this.records.find(function(model){
                    return _.indexOf(fromIds, model.get('fromRecordId')) >=0;
                }),
                at = this.records.indexOf(model);
            this.recordSelectorView.showDropdown(at, fromIds.length);
        },

        hideDropdown: function(){
            this.recordSelectorView.hideDropdown();
        },

        onRRSChange: function(){
            this.recordSelectorView.setSelectedMetafieldId(this.model.get('toMetaFieldId'));
        }
    });

    return RecodesAsideLayout;

});
