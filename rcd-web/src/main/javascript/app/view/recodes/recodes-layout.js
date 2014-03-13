'use strict';

define(function (require) {
    var _ = require('underscore'),
        $ = require('jquery'),
        Marionette = require('marionette'),
        Grid = require('grid/grid'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        ViewHelpers = require('view/view-helpers'),
        viewTemplate = require('text!template/recodes/recodes-layout.html');

    var windowEventNS = '.page_recodes';
    var DELETE_CELL_WIDTH = 48;

    /**
     * Recode rules layout.
     * Handles all the UI-only logic for recodes.
     * @class
     */
    var RecodesLayout = Marionette.Layout.extend({
        template: _.template(viewTemplate),
        regions: {
            recordsRegion: '[data-region="records"]',
            pagerRegion: '[data-region="pager"]',
            popupsRegion: '[data-region="popups"]'
        },
        ui: {
            filter: '[data-field="filter"]',
            selection: '[data-field="selection"]',
            selectionCount: '[data-field="selection-count"]',
            spacer: '.spacer'
        },
        templateHelpers: ViewHelpers.templateHelpers,
        events: {
            'click [data-action="unselect"]': 'onUnselectClick',
            'keypress [data-field="filter"]': 'filterKeyup',
            'click .searchbar .search': 'applyFilter',
            'click .searchbar .clear': 'clearFilter'
        },

        /* public */

        initialize: function (options) {
            this.columns = this.createColumns(options.metafields);
            this.records = options.records;

            this.createParts();
            this.listenTo(this.records, {
                'add remove reset': this.onListChanged,
                'backgrid:selected': this.onRecordSelected
            });
            this.listenTo(this.model, 'change', this.onRRSChange);
            $(window).on('resize' + windowEventNS, _.bind(this.onWindowResize, this));
        },
        showPopup: function (popup) {
            this.popupsRegion.show(popup);
        },

        resizeColumns: function () {
            this.grid.$container = this.grid.$el.closest('.table-wrapper');
            this.grid.resizeColumns();
            $(window).on('resize' + windowEventNS, _.bind(this.grid.resizeColumns, this.grid));
        },

        onShow: function () {
            this.recordsRegion.show(this.grid);
            this.pagerRegion.show(this.paginator);
            this.onWindowResize();
        },

        onClose: function () {
            $(window).off(windowEventNS);
        },

        getSelectedRecordIds: function(){
            return _.pluck(_.compact(this.grid.getSelectedModels()),'id');
        },

        unselectAllRecords: function(){
            this.records.each(function(model){
                model.trigger('backgrid:select', model, false);
            });
            this.trigger('recodes:selected', []);
        },

        /* private */

        createParts: function () {
            this.grid = new Grid.Grid({
                attributes: {
                    'data-control': 'recodes-from-table'
                },
                collection: this.records,
                columns: this.columns,
                body: Grid.Body,
                header: Grid.Header,
                emptyText: i18n.tc('view.records.empty')
            });
            this.paginator = new Grid.Paginator({
                attributes: {
                    'data-control': 'recodes-from-paginator'
                },
                collection: this.records,
                windowSize: 5
            });
            this.listenTo(this.records, 'state', this.onWindowResize);
        },

        /**
         * Creates columns based on metafields
         * @return {Backbone.Collection}
         */
        createColumns: function(metafields) {
            var columns = new Grid.Columns(null, {
                metafields: metafields,
                editable: false,
                cell: Grid.PlainStringCell,
                preColumns: [{
                    name: '',
                    cell: Backgrid.Extension.SelectRowCell,
                    headerCell: Grid.SelectAllHeaderCell,
                    sortable: false,
                    width: DELETE_CELL_WIDTH
                }],
                selectedId: this.model.get('fromMetaFieldId')
            });
            return columns;
        },

        /**
         * Performs UI reaction on records adding/removing
         */
        onListChanged: function(){
            $(window).scroll();
        },

        onRRSChange: function(){
            this.columns.setSelectedId(this.model.get('fromMetaFieldId'));
            this.grid.header.render();
        },

        onRecordSelected: (function(){
            var previousVisible = false,
                previousIds = [];
            return function(){
                var ids = this.getSelectedRecordIds(),
                    count = ids.length;
                this.ui.selection.toggle(!!count);
                this.ui.selectionCount.text(i18n.tc('view.recodes.selected', count, count));
                if (!_.isEqual(ids, previousIds)) {
                    this.trigger('recodes:selected', ids);
                    previousIds = ids;
                }
                if (previousVisible != !!count) {
                    previousVisible = !!count;
                    this.trigger('recodes:resize', this.ui.spacer);
                }
            }
        })(),

        onUnselectClick: function(){
            this.unselectAllRecords();
        },

        onWindowResize: function(){
            this.trigger('recodes:resize', this.ui.spacer);
        },

        filterKeyup: function(e){
            if (e && (e.keyCode || e.which) == 13) {
                this.applyFilter();
            }
        },
        applyFilter: function(){
            this.records.trigger('filter', this.ui.filter.val().trim());
        },
        clearFilter: function(){
            this.ui.filter.val('');
            this.applyFilter();
        }
    });

    return RecodesLayout;

});
