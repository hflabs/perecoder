"use strict";
define(function (require) {

    var _ = require('underscore'),
        Backbone = require('backbone'),
        Backgrid = require('backgrid'),
        i18n = require('locale/i18n'),
        Grid = require('grid/grid-body');

    Grid.HeaderCell = Backgrid.HeaderCell.extend({
        /** @property */
        events: {
            "click .sortable": "onClick"
        },

        initialize: function(options){
            Backgrid.HeaderCell.prototype.initialize.apply(this, arguments);
            this.row = options.row;
            this.listenTo(this.column, 'change', this.render);
        },

        isEditing: function(){
            return !!(this.row && this.row.editMode && this.column.get('writable'));
        },

        /**
         Event handler for the `click` event on the cell's anchor. If the column is
         sortable, clicking on the anchor will cycle through 3 sorting orderings -
         `ascending`, `descending`, and default.
         */
        onClick: function (e) {
            e && e.stopPropagation();
            var columnName = this.column.get("name");

            if (this.column.get("sortable")) {
                if (this.direction() === "ascending") {
                    this.sort(columnName, "descending", function (left, right) {
                        var leftVal = left.get(columnName);
                        var rightVal = right.get(columnName);
                        if (leftVal === rightVal) {
                            return 0;
                        }
                        else if (leftVal > rightVal) {
                            return -1;
                        }
                        return 1;
                    });
                }
                else {
                    this.sort(columnName, "ascending", function (left, right) {
                        var leftVal = left.get(columnName);
                        var rightVal = right.get(columnName);
                        if (leftVal === rightVal) {
                            return 0;
                        }
                        else if (leftVal < rightVal) {
                            return -1;
                        }
                        return 1;
                    });
                }
            }
        },

        /**
         Renders a header cell with a sorter and a label.
         */
        render: function () {
            this.$el.empty();
            this.$el.toggleClass('selected', !!this.column.get('selected'));

            var label = this.column.get("label"),
                $label = $('<div class="column-label">')
                    .text(label)
                    .attr('title', label)
                    .toggleClass('sortable', this.column.get('sortable'));
            this.$el.append($('<div/>').append($label));
            this.delegateEvents();
            return this;
        }

    });

    Grid.AddHeaderCell = Grid.HeaderCell.extend({
        className: 'add-header-cell',

        events: {
            'click [data-action="add-record"]:not([disabled])': 'onClick'
        },

        onClick: function (e) {
            e && e.stopPropagation();
            if (this.collection.state.writable !== false) {
                this.collection.trigger('backgrid:add');
            }
        },

        render: function () {
            this.$el.empty();
            if (this.collection.state.writable !== false) {
                this.$el.append(
                    $('<i class="icon-20 compact add interactive" data-action="add-record"></i>')
                        .attr('disabled', this.isEditing())
                );
                this.delegateEvents();
            }
            return this;
        }
    });

    Grid.AddColumnHeaderCell = Backgrid.HeaderCell.extend({
        className: 'add-column-cell',
        events: {
            'click span': 'onClick'
        },
        render: function(){
            this.$el.html('<span><i class="icon-20 compact structure-add interactive" data-action="structure-add"></i><span>');
            this.delegateEvents();
            return this;
        },
        onClick: function(){
            this.collection.trigger('backgrid:column:add');
        }
    });

    Grid.ApplyHeaderCell = Backgrid.HeaderCell.extend({
        className: 'apply-cell',
        events: {
            'click span': 'onClick'
        },
        render: function(){
            this.$el.html(
                $('<span><i class="icon-20 compact structure-ready interactive" data-action="structure-ready"></i> </span>')
                    .append(i18n.tc('view.records.edit-ready'))
            );
            this.delegateEvents();
            return this;
        },
        onClick: function(){
            this.collection.trigger('backgrid:restructured');
        }
    });

    Grid.EditableHeaderCell = Grid.HeaderCell.extend({
        className: 'editable-header-cell',
        events: {
            "click .sortable": "onClick",
            'click .structure': 'onStructureClick',
            'click [data-action="structure-delete"]': 'onStructureDeleteClick',
            'click [data-action="structure-add"]': 'onStructureAddClick',
            'change input': 'onInputChange',
            'keydown input': 'onInputKeydown',
            'click .unique': 'onUniqueClick'
        },

        /**
         * Checks if the cell is last in the row
         */
        isColumnLast: function(){
            return this.column.collection.at(this.column.collection.length - 1) == this.column;
        },

        /**
         Renders a header cell with a sorter and a label.
         */
        render: function () {
            if (this.isEditing()) {
                var metafield = this.column.get('model');
                this.$el.html(
                    _.template(
                        '<%if(deletable){%><span class="trash" data-action="structure-delete"><i class="icon-20 compact trash interactive"></i></span><%}%>' +
                            '<div class="editable-header-value">' +
                            '<input type="text"<%if(uniquable){%> class="btn-owner"<%}%> value="<%- label %>"<%if(maxlength){%> maxlength="<%- maxlength %>"<%}%>/>' +
                            '<%if(uniquable){%><i class="icon-16 unique<%if(!unique){%> not<%}%> interactive" title="<%- uniqueTooltip %>"></i><%}%>' +
                            '</div>',
                        {
                            label: this.column.get('label'),
                            maxlength: metafield.modelParams.fields.name.maxLength,
                            deletable: metafield.get('writable') && !metafield.get('primary'),
                            unique: metafield.get('unique'),
                            uniquable: !metafield.get('primary'),
                            uniqueTooltip: i18n.tc('model.metafield.unique')
                        }
                    )
                );
                this.delegateEvents();
            } else {
                Grid.HeaderCell.prototype.render.apply(this);
                // Puts button for structure editing
                if (this.collection.writable && this.isColumnLast()) {
                    this.$el.prepend($('<i class="icon-20 compact structure interactive" data-action="structure-edit"></i>'));
                }
            }

            return this;
        },

        onStructureClick: function(e){
            this.collection.trigger('backgrid:restructure');
        },

        onStructureDeleteClick: function(){
            this.collection.trigger('backgrid:column:delete', this.column);
        },

        onInputChange: function(e){
            this.setModelName($(e.target).val().trim());
        },

        onInputKeydown: function(e){
            switch(e.keyCode){
                case 13:
                    this.setModelName($(e.target).val().trim());
                    break;
                case 27:
                    this.collection.trigger('backgrid:restructured');
                    break
            }
        },

        setModelName: function (value) {
            var model = this.column.get('model');
            if (model) {
                this.column.set('label', value);
                model.safeSet('name', value, { saveNew: true });
            }
        },

        onUniqueClick: function(){
            var model = this.column.get('model');
            if (model) {
                var xhr = model.safeSet('unique', !model.get('unique'));
                if (xhr) {
                    xhr.done(_.bind(this.render, this));
                }
            }
        }

    });

    Grid.HeaderRow = Grid.Row.extend({
        requiredOptions: ["columns", "collection"],
        makeCell: function (column, options) {
            var headerCell = column.get("headerCell") || options.headerCell || Grid.HeaderCell;
            headerCell = new headerCell({
                column: column,
                collection: this.collection,
                row: this
            });
            return headerCell;
        }
    });

    Grid.Header = Backgrid.Header.extend({
        initialize: function (options) {
            Backgrid.requireOptions(options, ["columns", "collection"]);

            this.columns = options.columns;
            if (!(this.columns instanceof Backbone.Collection)) {
                this.columns = new Columns(this.columns);
            }

            this.row = new Grid.HeaderRow({
                columns: this.columns,
                collection: this.collection
            });
        },

        isInEditMode: function(){
            return this.row.editMode;
        },

        focusColumn: function(colIndex){
            var cell = this.row.cells[colIndex],
                $input = cell && cell.$(':text');
            $input && $input.focus();
        }
    });

    return Grid;
});