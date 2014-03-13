'use strict';

define(function (require) {
    var _ = require('underscore'),
        $ = require('jquery'),
        Marionette = require('marionette'),
        Grid = require('grid/grid'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        RecordModel = require('model/content/record-model'),
        ViewHelpers = require('view/view-helpers'),
        viewTemplate = require('text!template/content/content-layout.html');

    var windowEventNS = '.page_content';
    var DELETE_CELL_WIDTH = 48;

    /**
     * Dictionary layout.
     * Handles all the UI-only logic for dictionaries' content.
     * @class
     */
    var ContentLayout = Marionette.Layout.extend({
        template: _.template(viewTemplate),
        regions: {
            recordsRegion: '[data-region="records"]',
            pagerRegion: '[data-region="pager"]',
            popupsRegion: '[data-region="popups"]'
        },
        ui: {
            addBtn: '[data-action="add"]',
            deleteBtn: '[data-action="delete"]',
            filter: '[data-field="filter"]'
        },
        templateHelpers: ViewHelpers.templateHelpers,
        events: {
            'keypress [data-field="filter"]': 'filterKeyup',
            'click .searchbar .search': 'applyFilter',
            'click .searchbar .clear': 'clearFilter',
            'click [data-action="download"]': 'onDownloadClick'
        },
        triggers: {
            'click [data-action="upload"]': 'records:upload'
        },

        /* public */

        initialize: function () {
            this.columns = this.createColumns();
            this.listenTo(this.model.metafields, {
                'add': this.onMetafieldAdd,
                'remove': this.onMetafieldRemove,
                'reset': this.onMetafieldReset,
                'change': this.onMetafieldChange
            });
            this.createParts();
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
        },
        onClose: function () {
            $(window).off(windowEventNS);
        },

        showCreatingRecord: function(){
            var NEW_MODEL_POSITION = 0;
            var model = this.model.recordsList.at(NEW_MODEL_POSITION);

            if (model && model.isNew())
                return;

            model = new RecordModel();
            this.grid.body.insertRow(model, {
                at: NEW_MODEL_POSITION
            });
            model.trigger('backgrid:startedit');
            model.trigger('backgrid:row:focus');

        },

        showColumnsEditor: function(){
            this.grid.header.row.enterEditMode();
            this.addColumn({
                name: '',
                cell: Grid.FakeCell,
                headerCell: Grid.AddColumnHeaderCell,
                width: 38
            });
            this.addColumn({
                name: '',
                cell: Grid.FakeCell,
                headerCell: Grid.ApplyHeaderCell,
                width: 100
            });
        },

        hideColumnsEditor: function(){
            this.grid.header.row.exitEditMode();
            this.removeColumn(this.columns.slice(-2));
        },

        addColumn: function(model, options){
            this.columns.add(model, options);
            this.grid.render();
            this.grid.resizeColumns();
        },
        removeColumn: function(model){
            this.columns.remove(model);
            this.grid.render();
            this.grid.resizeColumns();
        },

        /* private */

        createParts: function () {
            this.grid = new Grid.Grid({
                attributes: {
                    'data-control': 'records-main-table'
                },
                collection: this.model.recordsList,
                columns: this.columns,
                body: Grid.Body,
                header: Grid.Header,
                emptyText: i18n.tc('view.records.empty')
            });
            this.paginator = new Grid.Paginator({
                attributes: {
                    'data-control': 'records-main-paginator'
                },
                collection: this.model.recordsList,
                windowSize: 5
            });
        },

        /**
         * Creates columns based on this.metafields
         * @return {Backbone.Collection}
         */
        createColumns: function () {
            var columns = new Backgrid.Columns();
            this.resetColumns(columns);
            return columns;
        },

        /**
         * Intercept default download behavour to route errors to notifiactions list
         * @param e
         */
        onDownloadClick: function(e){
            e.preventDefault();
            var url = $(e.currentTarget).prop('href'),
                self = this;
            $.fileDownload(url, {
                cookieName: 'success',
                failCallback: function (html, url) {
                    self.trigger('records:download:error');
                }
            });
        },

        /**
         * Inserts first column with 'delete' buttons
         */
        resetColumns: function(columns){
            columns.reset();
            columns.add({
                name: '',
                cell: Grid.DeleteRowCell,
                headerCell: Grid.AddHeaderCell,
                editable: this.model.currentDictionary.isWritable(),
                sortable: false,
                width: DELETE_CELL_WIDTH,
                writable: true
            })
        },

        /**
         * Parses Metafield to columns attributes
         * @param model
         * @return {Object}
         */
        getColumnData: function(model){
            return {
                model: model,
                name: model.id || model.cid,
                cell: Grid.OptionalStringCell,
                headerCell: Grid.EditableHeaderCell,
                label: model.get('name'),
                writable: model.get('writable'),
                editable: this.model.currentDictionary.isWritable()
            }
        },

        getColomnOfMetafield: function(model){
            return this.columns.where({model: model})[0];
        },

        /**
         * Creates column
         * @param {Object} Metafield model
         */
        onMetafieldAdd: function(model){
            if (!model.get('hidden')) {
                var column = this.getColumnData(model);
                var at = this.columns.length;
                if (this.grid.header.isInEditMode()) {
                    at -= 2;
                }
                this.addColumn(column, { at: at });
                this.grid.header.focusColumn(at);
            }
        },

        /**
         * Removes column
         */
        onMetafieldRemove: function(model){
            var column = this.getColomnOfMetafield(model);
            this.removeColumn(column);
        },

        /**
         * Reset columns
         */
        onMetafieldReset: function(){
            this.resetColumns(this.columns);
            this.model.metafields.each(function(model){
                if (!model.get('hidden')) {
                    this.columns.add(this.getColumnData(model));
                }
            }, this);
            this.grid.header.render();
            this.grid.resizeColumns();
        },

        /**
         * Updates column
         * @param model
         */
        onMetafieldChange: function(model){
            var column = this.getColomnOfMetafield(model),
                attrs = this.getColumnData(model);
            column.set(attrs);
        },

        filterKeyup: function(e){
            if (e && (e.keyCode || e.which) == 13) {
                this.applyFilter();
            }
        },
        applyFilter: function(){
            this.model.recordsList.trigger('filter', this.ui.filter.val().trim());
        },
        clearFilter: function(){
            this.ui.filter.val('');
            this.applyFilter();
        }
    });

    return ContentLayout;

});
