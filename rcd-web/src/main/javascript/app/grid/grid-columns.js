"use strict";
define(function (require) {

    var _ = require('underscore'),
        Backgrid = require('backgrid'),
        Grid = require('grid/base-grid');

    /**
     * Columns collection, based on Metafileds
     * @type {*|void}
     */
    Grid.Columns = Backgrid.Columns.extend({

        initialize: function(models,options){
            Backgrid.requireOptions(options, ['metafields']);
            this.sortable = options.sortable;
            this.editable = !!options.editable;
            this.selectedId = options.selectedId;
            this.metafields = options.metafields;
            this.preColumns = options.preColumns || [];
            this.cell = options.cell;
            this.listenTo(this.metafields, {
                'add': this.onMetafieldAdd,
                'remove': this.onMetafieldRemove,
                'reset': this.onMetafieldReset,
                'change': this.onMetafieldChange
            });
            this.onMetafieldReset(this.metafields);
        },

        setSelectedId: function(id){
            this.selectedId = id;
            this.each(function(model){
                model.set('selected', model.get('name') == id);
            });
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
                cell: this.cell || Grid.OptionalStringCell,
                headerCell: Grid.EditableHeaderCell,
                label: model.get('name'),
                writable: model.get('writable'),
                editable: this.editable,
                sortable: this.sortable,
                selected: model.id == this.selectedId
            }
        },

        findColomn: function(model){
            return this.where({name: model.id})[0];
        },

        addColumn: function(model, options){
            if (!model.get('hidden')) {
                var columnData = this.getColumnData(model);
                this.add(columnData, options);
            }
        },

        onMetafieldAdd: function(model, collection, options){
            this.addColumn(model);
        },

        onMetafieldRemove: function(model, collection, options){
            var column = this.findColomn(model);
            if (column) {
                this.remove(column, options);
            }
        },

        onMetafieldReset: function(collection, options){
            this.reset(this.preColumns, { silent: true });
            collection.each(function(model){
                this.addColumn(model, { silent: true });
            }, this);
            this.trigger('reset', this, options);
        },

        onMetafieldChange: function(model){
            var column = this.findColomn(model);
            if (column) {
                column.set(this.getColumnData(model));
            }
        }

    });

    return Grid;

});