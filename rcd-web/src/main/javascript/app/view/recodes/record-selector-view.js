'use strict';

define(function(require){
    var _ = require('underscore'),
        Grid = require('grid/grid'),
        i18n = require('locale/i18n'),
        Marionette = require('marionette'),
        ViewHelpers = require('view/view-helpers'),
        viewTemplate = require('text!template/recodes/record-selector.html');

    /**
     * Widget for selecting record of dictionary.
     * @class
     */
    var RecodeDropdownView = Marionette.Layout.extend({
        template: _.template(viewTemplate),
        templateHelpers: function(){
            var self = this;
            return _.extend({
                getResetText: function(){
                    return self.resetText;
                }
            }, ViewHelpers.templateHelpers)
        },
        regions: {
            recordsRegion: '[data-region="records"]',
            dropdownRegion: '[data-region="dropdown"]',
            paginatorRegion: '[data-region="pager"]'
        },
        events: {
            'click [data-action="reset"]': 'onResetRule',
            'keydown .searchbar [data-field="filter"]': 'onFilterKeydown',
            'click .searchbar .search': 'onApplyFilter',
            'click .searchbar .clear': 'onClearFilter',
            'click .dropdown-selection': 'onSelectionClick'
        },
        ui: {
            filter: '.searchbar [data-field="filter"]',
            dropdownFilter: '[data-field="filter"]',
            dropdown: '.target-dropdown',
            dropdownSelection: '.dropdown-selection',
            dropdownTableWrapper: '.dropdown-table-wrapper'
        },
        initialize: function(options){
            this.metafields = options.metafields;

            // Records displayed in table
            this.records = options.records;
            this.listenTo(this.records, {
                'backgrid:row:click': this.onRowClick,
                'sync reset': this.resizeColumns
            });

            // records displayed in dropdown
            this.dropdownRecords = options.dropdownRecords;

            this.selectedMetafieldId = options.selectedMetafieldId;

            this.$container = options.$container || $('body');
            this.listenTo(this.dropdownRecords, {
                'sync': this.onDropdownRecordsSync,
                'backgrid:row:click': this.onDropdownRowClick
            });

            this.resetText = options.resetText;

            this.grid = this.createGrid();
            this.dropdownGrid = this.createDropdownGrid();
            this.paginator = this.createPaginator();
        },

        onClose: function(){
            this.unbindDropdownEvents();
        },
        onShow: function(){
            this.recordsRegion.show(this.grid);
            this.dropdownRegion.show(this.dropdownGrid);
            this.paginatorRegion.show(this.paginator);
            this.bindDropdownEvents();
            this.bindScrollEvents();
        },

        createGrid: function(){
            var grid = new Grid.Grid({
                collection: this.records,
                columns: new Grid.Columns(null, {
                    metafields: this.metafields,
                    editable: false,
                    cell: Grid.BorderedStringCell,
                    selectedId: this.selectedMetafieldId,
                    sortable: false
                }),
                body: Grid.Body,
                header: Grid.Header,
                emptyText: i18n.tc('view.records.empty')
            });
            return grid;
        },

        createDropdownGrid: function(){
            var grid = new Grid.Body({
                collection: this.dropdownRecords,
                columns: new Grid.Columns(null, {
                    metafields: this.metafields,
                    editable: false,
                    cell: Grid.PlainStringCell
                }),
                row: Grid.Row,
                emptyText: i18n.tc('view.records.empty')
            });
            return grid;
        },

        createPaginator: function(){
            var view = new Grid.Paginator({
                collection: this.dropdownRecords,
                windowSize: 5
            });
            //this.dropdownRecords.state.pageSize = 5;
            return view;
        },

        resizeColumns: function () {
            this.grid.$container = this.grid.$el.closest('.table-wrapper');
            this.grid.resizeColumns();
        },

        setSelectedMetafieldId: function(toMetafieldId){
            this.grid.columns.setSelectedId(toMetafieldId);
            this.grid.header.render();
        },

        bindDropdownEvents: function(){
            var dropdownClicked = false,
                $list = this.ui.dropdown,
                self = this,
                handler = function(){
                    if ($list.length && $list.is(':visible') && !dropdownClicked) {
                        if ($list.data('opening')) {
                            $list.data('opening', false);
                        } else {
                            self.records.trigger('recordselector:unselect');
                        }
                    }
                    dropdownClicked = false;
                };

            this.$container.on('click.'+this.cid, handler)
                .on('keydown', function(e){
                    if (e.keyCode == 27) {
                        handler();
                    }
                });

            $list.on('click.'+this.cid, function(){
                dropdownClicked = true;
            });
        },

        unbindDropdownEvents: function(){
            this.$container.off('.' + this.cid);
        },

        bindScrollEvents: function(){
            var $dropdownTableWrapper = this.ui.dropdownTableWrapper;
            this.recordsRegion.$el.on('scroll', function(e){
                $dropdownTableWrapper.scrollLeft($(e.target).scrollLeft());
            });
        },

        /**
         * Opens dropdown at specified row
         * @param at index of row
         */
        showDropdown: function(at, selected){
            at = Math.max(at, 0);
            var $row = this.grid.body.rows[at].$el,
                rowTop = $row.offset().top - this.grid.$el.offset().top;
            if (selected == 1) {
                rowTop += $row.outerHeight();
            }
            $row.addClass('hover')
                .siblings().removeClass('hover').addClass('faded');
            this.ui.dropdown.css('top', rowTop)
                .show();
            this.ui.filter.focus();

            // set data attribute until current event stack cleared
            _.defer(function($list){
                $list.data('opening', false);
            }, this.ui.dropdown.data('opening', true));

            this.ui.dropdownSelection
                .toggle(selected > 1)
                .text(i18n.tc('view.recodes.matchselection', selected, selected));

            this.dropdownRecords.fetch();
        },

        hideDropdown: function(){
            this.ui.dropdown.hide();
            _.each(this.grid.body.rows, function(row){
                row.$el.removeClass('hover').removeClass('faded');
            });
        },

        /**
         * Handles user's click on table row
         * @param {RecordModel|EmptyModel} model
         * @param {jQuery} row node
         */
        onRowClick: function(model, $row){
            if ($row.hasClass('hover')) {
                this.records.trigger('recordselector:unselect');
            } else {
                this.records.trigger('recordselector:select', model);
            }
        },

        /**
         * Handles user's selection
         * @param {RecordModel} model
         */
        onDropdownRowClick: function(model){
            this.dropdownRecords.trigger('recordselector:selected', model.id);
        },

        onResetRule: function(){
            // Emulate click on droprown table row
            this.dropdownRecords.trigger('recordselector:selected', null);
        },

        /**
         * Format dropdown table
         *
         * Copies cell widths to dropdown table
         */
        onDropdownRecordsSync: function(){
            this.ui.dropdown.find('table').width( this.grid.$el.width() );
            var headerCells = this.grid.header.row.cells;
            _.each(headerCells, function(cell, index){
                var width = cell.$el.outerWidth();
                this.ui.dropdown.find('td:nth-child(' + (index +1) + ')').outerWidth(width);
            }, this);
        },

        onSelectionClick: function(){
            this.records.trigger('recordselector:unselect');
        },

        applyDropdownFilter: function(){
            this.dropdownRecords.trigger('filter', this.ui.dropdownFilter.val().trim());
        },
        onFilterKeydown: function(e){
            if (e && (e.keyCode || e.which) == 13) {
                this.applyDropdownFilter();
            }
        },
        onApplyFilter: function(){
            this.applyDropdownFilter();
        },
        onClearFilter: function(){
            this.ui.dropdownFilter.val('');
            this.applyDropdownFilter();
        }

    });

    return RecodeDropdownView;

});
