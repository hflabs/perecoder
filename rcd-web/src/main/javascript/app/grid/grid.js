"use strict";
define(function(require){

    var _ = require('underscore'),
        Backgrid = require('backgrid'),
        i18n = require('locale/i18n'),
        Grid = require('grid/base-grid');

    require('grid/grid-body');
    require('grid/grid-cells');
    require('grid/grid-header');
    require('grid/grid-columns');
    require('grid/grid-paginator');
    require('grid/grid-select-all');

    Grid.Grid = Backgrid.Grid.extend({
        className: 'backgrid table',

        resizeColumns: function(){
            var CELL_MIN_WIDTH = 200;
            var rows = this.body.rows;
            if (!rows[0]) {
                return;
            }
            var widths = [], fixedWidths = [], width = 0;
            var freeSpace = (this.$container || this.$el).width();
            // 1. fill widths with biggest lengths of cell content
            this.columns.each(function(col, colIndex){
                var colWidth = col.get('width');
                if (colWidth) {
                    fixedWidths[colIndex] = colWidth;
                    freeSpace -= colWidth;
                } else {
                    width += widths[colIndex] = _.max(_.map(rows, function(row){
                        var cells = row.cells;
                        return cells && +cells[colIndex].$el.attr('data-text-length') || 0;
                    }));
                }
            });
            // 2. zoom widths to fill all free space
            var ratio = freeSpace / width;
            if (ratio) {
                widths = _.map(widths, function(width){
                    return width * ratio;
                });
            }
            // 3. widen narrow columns
            var colIndexesWided = [];
            var widthToDistribute = 0;
            while (
                    _.any(widths, function(width, colIndex){
                        if (width < CELL_MIN_WIDTH) {
                            widthToDistribute = CELL_MIN_WIDTH - width;
                            widths[colIndex] = CELL_MIN_WIDTH;
                            colIndexesWided.push(colIndex);
                            return true;
                        }
                        return false;
                    })
                    ) {
                var colsToNarrowIndexes = [];
                _.each(widths, function(width, colIndex){
                    if (_.isNumber(width) && !_.isNaN(width) && colIndexesWided.indexOf(colIndex) < 0) {
                        colsToNarrowIndexes.push(colIndex);
                    }
                });
                if (colsToNarrowIndexes.length > 0) {
                    var narrowBy = widthToDistribute / colsToNarrowIndexes.length;
                    _.each(colsToNarrowIndexes, function(colIndex){
                        widths[colIndex] -= narrowBy;
                    });
                }
            }
            // 4. set cells width
            _.each(this.header.row.cells, function(cell, colIndex){
                var width = widths[colIndex] || fixedWidths[colIndex];
                if (width) {
                    cell.$el.css({width: width})
                }
            })
            // 5. modifying container's overflow property
            if (this.$container) {
                this.$container.css('overflow', this.$container.width() < this.$el.width() ? 'auto': 'hidden');
            }
        },

        getSelectedModels: function () {
            var selectAllHeaderCell;
            var headerCells = this.header.row.cells;
            for (var i = 0, l = headerCells.length; i < l; i++) {
                var headerCell = headerCells[i];
                if (headerCell instanceof Grid.SelectAllHeaderCell) {
                    selectAllHeaderCell = headerCell;
                    break;
                }
            }

            var result = [];
            if (selectAllHeaderCell) {
                for (var modelId in selectAllHeaderCell.selectedModels) {
                    result.push(this.collection.get(modelId));
                }
            }

            return result;
        }
    });

    return Grid;
});
