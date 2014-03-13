"use strict";
define(function (require) {

    var _ = require('underscore'),
        $ = require('jquery'),
        Backgrid = require('backgrid'),
        i18n = require('locale/i18n'),
        ViewHelpers = require('view/view-helpers'),
        Grid = require('grid/base-grid');

    Grid.GroupNameFormatter = function (collection) {
        this.collection = collection;
    };
    Grid.GroupNameFormatter.prototype = new Backgrid.CellFormatter();
    _.extend(Grid.GroupNameFormatter.prototype, {
        fromRaw: function (rawValue) {
            if (_.isUndefined(rawValue) || _.isNull(rawValue) || !this.collection || !this.collection.get(rawValue)) return '';
            return '[' + this.collection.get(rawValue).get('name') + ']';
        }
    });

    Grid.OptionalStringFormatter = function () {};
    Grid.OptionalStringFormatter.prototype = new Backgrid.CellFormatter();
    _.extend(Grid.OptionalStringFormatter.prototype, {
        fromRaw: function (rawValue) {
            if (_.isUndefined(rawValue) || _.isNull(rawValue)) {
                return '<span class="visible-in-hover">' + i18n.tc('view.dictionaries.description.add') + '</span>';
            }
            return '[' + rawValue + ']';
        }
    });

    Grid.InputCellEditor = Backgrid.InputCellEditor.extend({
        /**
         Renders a text input with the cell value formatted for display, if it
         exists.
         */
        render: function () {
            this.$el.val(this.formatter.fromRaw(this.model.get(this.column.get("name"))))
                .attr('maxlength', this.column.get('maxlength'));
            return this;
        },

        /**
         * Overrides standard focusing
         */
        postRender: function(model, column){
            if (!model.isNew()) {
                Backgrid.InputCellEditor.prototype.postRender.apply(this, arguments);
            }
            return this;
        },

        /**
         If the key pressed is `enter`, `tab`, `up`, or `down`, converts the value
         in the editor to a raw value for saving into the model using the formatter.

         If the key pressed is `esc` the changes are undone.

         If the editor goes out of focus (`blur`) but the value is invalid, the
         event is intercepted and cancelled so the cell remains in focus pending for
         further action. The changes are saved otherwise.

         Triggers a Backbone `backgrid:edited` event from the model when successful,
         and `backgrid:error` if the value cannot be converted. Classes listening to
         the `error` event, usually the Cell classes, should respond appropriately,
         usually by rendering some kind of error feedback.

         @param {Event} e
         */
        saveOrCancel: function (e) {

            var formatter = this.formatter;
            var model = this.model;
            var column = this.column;

            var command = new Backgrid.Command(e);
            var blurred = e.type === "blur";

            if (command.moveUp() || command.moveDown() || command.moveLeft() || command.moveRight() ||
                command.save() || blurred) {

                if (!model.isNew() || !command.moveLeft() && !command.moveRight()) {
                    e.preventDefault();
                    e.stopPropagation();
                }

                var val = this.$el.val();
                var newValue = formatter.toRaw(val).trim();
                if (_.isUndefined(newValue)) {
                    model.trigger("backgrid:error", model, column, val);
                }
                else {
                    var attr = column.get("name");
                    if (model.isNew()) {
                        model.set(attr, newValue);
                    } else {
                        model.trigger("backgrid:set", attr, newValue);
                    }
                    model.trigger("backgrid:edited", model, column, command);
                }
            }
            // esc
            else if (command.cancel()) {
                // undo
                e.stopPropagation();
                model.trigger("backgrid:edited", model, column, command);
            }
        }
    });

    Grid.Cell = Backgrid.Cell.extend({
        initialize: function(options){
            Backgrid.Cell.prototype.initialize.apply(this, arguments);
            this.row = options.row;
            this.editMode = !!(this.row && this.row.editMode);
        },
        editor: Grid.InputCellEditor,

        /**
         * Determines if cell can enter to edit mode
         * @return {*|boolean}
         */
        isEditable: function(){
            return this.column.get("editable")
                && (!this.model || !this.model.isWritable || this.model.isWritable());
        },

        isEditing: function(){
            return !!(this.row && this.row.editMode || this.editMode);
        },
        isColumnLast: function() {
            var realColumns = this.column.collection.filter(function(column){
                return column.get('name');
            });
            return realColumns.pop() == this.column;
        },
        enterEditMode: function (e) {
            if (this.isEditable()) {
                e && e.stopPropagation && e.stopPropagation();
                this.editMode = true;
                this.model.trigger("backgrid:edit", this.model, this.column, this, this.currentEditor);
                this.render();
                this.model.trigger("backgrid:editing", this.model, this.column, this, this.currentEditor);
            }
        },
        exitEditMode: function(){
            this.editMode = false;
            this.render();
        },
        applyEditMode: function(e){
            var $el = $(e.target);
            if ($el.attr('disabled')) {
                return;
            }
            this.model.trigger('backgrid:beforeapply', this.model, $el);
            this.model.trigger('backgrid:apply', this.model, $el);
        },
        render: function () {
            this.$el.empty();
            var $cellValue = $('<div class="cell-value"></div>');

            if (this.isEditing()) {
                // render in edit mode
                this.$el.addClass("editor");
                if (!this.currentEditor && this.editor) {
                    this.currentEditor = new this.editor({
                        column: this.column,
                        model: this.model,
                        formatter: this.formatter
                    });
                }
                if (this.currentEditor) {
                    $cellValue.append(this.currentEditor.$el);
                    this.currentEditor.render();
                }

                // button for last cell when create model
                if (this.model.isNew() && this.isColumnLast()) {
                    this.$el.append(
                        $('<span class="ready"><i class="icon-20 compact save"></i> </span>')
                            .append(i18n.tc('view.records.edit-ready'))
                    );
                }
            } else {
                // render in view mode
                this.$el.removeClass("editor");
                if (this.currentEditor) {
                    this.stopListening(this.currentEditor);
                    delete this.currentEditor;
                }
                var cellText = this.formatter.fromRaw(this.model.get(this.column.get("name"))) || '';
                this.$el.attr('data-text-length', cellText.length);
                if (this.renderValue) {
                    this.renderValue($cellValue, cellText);
                } else {
                    $cellValue.text(cellText);
                }
            }
            var isValueEmpty = !$cellValue.html();
            if (this.iconClassName && !isValueEmpty) {
                this.$el.append(
                    $('<i class="icon-16 cell-icon"></i>')
                        .addClass(this.iconClassName)
                        .toggleClass('locked', this.model.isReadonly())
                        .attr('data-action', this.isIconEditable && this.isEditable() ? 'edit' : '')
                );
                $cellValue.addClass('iconed');
            }
            if (isValueEmpty && this.renderEmpty) {
                this.renderEmpty($cellValue);
            }

            this.$el.append($cellValue);

            this.delegateEvents();
            return this;
        }
    });

    // Cells for recodes tables
    Grid.BorderedStringCell = Grid.Cell.extend({
        className: 'bordered-string-cell',
        events: {},
        editor: null,
        enterEditMode: function(){},
        exitEditMode: function(){},
        render: function(){
            var cellText = this.model.get(this.column.get('name')) || '';
            this.$el.attr('data-text-length', cellText.length)
                .html(_.template('<div><i class="icon-20 compact dropdown-colour pull-right"></i><%if(text){%><%- text %><%}else{%><span class="empty-cell-text"><%-emptyText%></span><%}%></div>',{
                    text: cellText,
                    emptyText: '<' + i18n.tc('view.recodes.cell.empty') + '>'
                }));
            return this;
        }
    });

    Grid.PlainStringCell = Grid.Cell.extend({
        className: 'plain-string-cell',
        events: {},
        editor: null,
        enterEditMode: function(){},
        exitEditMode: function(){},
        render: function(){
            var cellText = this.model.get(this.column.get('name')) || '';
            this.$el.attr('data-text-length', cellText.length)
                .html(_.template('<%if(isColumnLast){%><i class="icon-20 compact pin" title="<%- pinTitle %>"></i><%}%><div><%if(text){%><%- text %><%}else{%><span class="empty-cell-text"><%-emptyText%></span><%}%></div>',{
                    text: cellText,
                    emptyText: '<' + i18n.tc('view.recodes.cell.empty') + '>',
                    isColumnLast: this.isColumnLast(),
                    pinTitle: i18n.tc('view.recodes.pin.title')
                }));
            return this;
        }
    });

    // Cells for content tables
    Grid.StringCell = Grid.Cell.extend({
        className: 'string-cell',
        events: {
            'click [data-action="edit"]': 'enterEditMode',
            'click .ready': 'applyEditMode'
        },
        renderValue: function ($cellValue, cellText) {
            $cellValue.append($('<span class="cell-value-inner"/>').text(cellText));
            if (this.isEditable()) {
                $cellValue.append('<span class="visible-in-hover"><i class="icon-16 edit interactive" data-action="edit"></i></span>');
            }
            this.delegateEvents();
            return this;
        }

    });

    Grid.OptionalStringCell = Grid.Cell.extend({
        className: 'optional-cell',
        formatter: new Backgrid.StringFormatter(),
        isIconEditable: true,
        events: {
            'click [data-action="edit"]': 'enterEditMode',
            'click .ready': 'applyEditMode'
        },
        renderValue: function ($cellValue, cellText) {
            if (cellText) {
                $cellValue.append(
                    $('<span/>')
                        .text(cellText)
                        .toggleClass('edit', this.isEditable())
                        .attr({
                            'data-action': (this.isEditable() ? 'edit' : ''),
                            'title': cellText
                        })
                );
            }
        },
        renderEmpty: function ($cellValue) {
            if (this.isEditable()) {
                $cellValue.append(
                    $('<span class="visible-in-hover"/>')
                        .append(
                            $('<i class="icon-glyph add" data-action="edit"></i>'),
                            $('<span class="edit" data-action="edit"/>').text(this.emptyText || i18n.tc('view.grid.add'))
                        )
                );
            }
        }
    });

    Grid.DeleteRowCell = Grid.Cell.extend({
        className: 'delete-cell',

        events: {
            'click .delete': 'onClick'
        },

        onClick: function (e) {
            e && e.stopPropagation();
            if (this.isEditable()) {
                this.model.trigger('backgrid:delete', this.model);
            }
        },

        enterEditMode: function(){},

        render: function () {
            this.$el.empty();
            if (this.isEditable() && !this.model.isNew()) {
                this.$el.append($('<span class="visible-in-hover"><i class="icon-16 delete interactive"></i></span>'));
                this.delegateEvents();
            }
            return this;
        }
    });

    Grid.UriCell = Grid.Cell.extend({

        className: 'uri-cell',

        events: {
            'click i.edit': 'enterEditMode'
        },

        renderValue: function ($cellValue, cellText) {
            var href = _.template(this.hrefTemplate || '', _.extend({}, this.model.attributes, ViewHelpers.templateHelpers));
            $cellValue.append($('<a/>', {
                'class': 'cell-value-inner',
                title: cellText,
                href: href
            }).text(cellText));
            if (this.isEditable()) {
                $cellValue.append('<span class="visible-in-hover"><i class="icon-16 edit interactive"></i></span>');
            }
            this.delegateEvents();
            return this;
        }

    });

    Grid.FakeCell = Grid.Cell.extend({
        events: {},
        editor: null,
        enterEditMode: function(){},
        exitEditMode: function(){},
        render: function(){ return this; }
    });

    return Grid;
});
