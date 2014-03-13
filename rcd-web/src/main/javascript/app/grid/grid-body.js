"use strict";
define(function(require){

    var _ = require('underscore'),
        Backbone = require('backbone'),
        Backgrid = require('backgrid'),
        i18n = require('locale/i18n'),
        EmptyModel = require('model/common/empty-model'),
        Grid = require('grid/base-grid');

    Grid.Row = Backgrid.Row.extend({
        /**
         Initializes a row view instance.

         @param {Object} options
         @param {Backbone.Collection.<Backgrid.Column>|Array.<Backgrid.Column>|Array.<Object>} options.columns Column metadata.
         @param {Backbone.Model} options.model The model instance to render.

         @throws {TypeError} If options.columns or options.model is undefined.
         */
        initialize: function () {

            Backgrid.Row.prototype.initialize.apply(this, arguments);

            this.editMode = false;

            if (this.model) {
                this.$el.toggleClass('locked', this.model.isReadonly && this.model.isReadonly());

                this.listenTo(this.model, {
                    'backgrid:selected': this.onSelected,
                    'backgrid:row:focus': this.focusFirstControl,
                    'backgrid:startedit': this.enterEditMode,
                    'backgrid:stopedit': this.exitEditMode,
                    'backgrid:beforeapply': this.onBeforeApply,
                    'backgrid:pin': this.onPin
                });
            }
        },

        events: {
            'click': 'onRowClick'
        },

        makeCell: function (column) {
            return new (column.get("cell"))({
                column: column,
                model: this.model,
                row: this
            });
        },

        onRowClick: function(e){
            var $el = $(e.target);
            if (this.model && !$el.is('input,textarea,select')) {
                e.preventDefault();
                e.stopPropagation();
                this.model.trigger('backgrid:row:click', this.model, this.$el, $el);
            }
        },

        onSelected: function(model, checked){
            this.$el.toggleClass('checked', checked)
        },

        switchEditMode: function(edit){
            this.editMode = edit;
            this.$el.toggleClass('editing', edit);
            this.render();
        },

        enterEditMode: function(){
            this.switchEditMode(true);
        },

        exitEditMode: function(){
            this.switchEditMode(false);
        },

        onBeforeApply: function(){
            this.$('input,textarea,select').blur();
        },

        focusFirstControl: function(){
            this.$('input,textarea,select').first().focus();
        },

        onPin: function(pin){
            this.$el.toggleClass('pinned', pin);
        }
    });

    Grid.EmptyModelRow = Backbone.View.extend({
        tagName: 'tr',
        className: 'emptymodel',
        emptyText: i18n.tc('view.recodes.unmatched'),
        events: {
            'click': 'onRowClick'
        },
        render: function(){
            this.$el.empty();
            this.$el.append(
                $(_.template('<td colspan="<%= colspan %>"><div><i class="icon-20 compact dropdown-colour pull-right"></i><%- text %></div></td>', {
                    colspan: this.columns.length,
                    text: this.model.get('text') || this.emptyText
                }))
            );
            return this;
        },
        initialize: function(options){
            this.columns = options.columns;
            this.listenTo(this.columns, 'add remove reset', this.onColumnsUpdated);
        },

        onColumnsUpdated: function(){
            this.$('td').attr('colspan', this.columns.length);
        },

        onRowClick: function(e){
            var $el = $(e.target);
            if (this.model && !$el.is('input,textarea,select')) {
                e.preventDefault();
                e.stopPropagation();
                this.model.trigger('backgrid:row:click', this.model, this.$el, $el);
            }
        }

    });

    Grid.EmptyRow = Backbone.View.extend({

        /** @property */
        tagName: "tr",

        /** @property */
        emptyText: null,

        /**
         Initializer.

         @param {Object} options
         @param {string} options.emptyText
         @param {Backbone.Collection.<Backgrid.Column>|Array.<Backgrid.Column>|Array.<Object>} options.columns Column metadata.
         */
        initialize: function (options) {
            Backgrid.requireOptions(options, ["emptyText", "columns"]);

            this.emptyText = options.emptyText;
            this.columns =  options.columns;
            this.listenTo(this.columns, 'all', this.render);
        },

        /**
         Renders an empty row.
         */
        render: function () {
            this.$el.empty();

            var td = document.createElement("td");
            td.setAttribute("colspan", this.columns.length);
            td.textContent = this.emptyText;

            this.el.setAttribute("class", "empty");
            this.el.appendChild(td);

            return this;
        }
    });
    Grid.Body = Backgrid.Body.extend({

        initialize: function (options) {
            Backgrid.requireOptions(options, ["columns", "collection"]);

            this.columns = options.columns;
            if (!(this.columns instanceof Backbone.Collection)) {
                this.columns = new Backgrid.Columns(this.columns);
            }

            this.row = options.row || Grid.Row;
            this.rows = this.collection.map(function (model) {
                var rowClass = model instanceof EmptyModel ? Grid.EmptyModelRow : this.row;
                var row = new rowClass({
                    columns: this.columns,
                    model: model
                });

                return row;
            }, this);

            this.emptyText = options.emptyText;
            this.loadingText = options.loadingText || i18n.tc('view.grid.loading');
            this._unshiftEmptyRowMayBe();

            var collection = this.collection;
            this.listenTo(collection, "add", this.insertRow);
            this.listenTo(collection, "remove", this.removeRow);
            this.listenTo(collection, "sort", this.refresh);
            this.listenTo(collection, "reset", this.refresh);
            this.listenTo(collection, "backgrid:edited", this.moveToNextCell);
            this.listenToOnce(collection, 'sync reset', this.updateEmptyText);
        },

        insertRow: function (model, collection, options) {

            if (this.rows[0] instanceof Grid.EmptyModelRow
                || this.rows[0] instanceof Grid.EmptyRow) this.rows.pop().remove();

            // insertRow() is called directly
            if (!(collection instanceof Backbone.Collection) && !options) {
                this.collection.add(model, (options = collection));
                return;
            }

            options = _.extend({render: true}, options || {});

            var rowClass = model instanceof EmptyModel ? Grid.EmptyModelRow : this.row;
            var row = new rowClass({
                columns: this.columns,
                model: model
            });

            var index = collection.indexOf(model);
            this.rows.splice(index, 0, row);

            var $el = this.$el;
            var $children = $el.children();
            var $rowEl = row.render().$el;

            if (options.render) {
                if (index >= $children.length) {
                    $el.append($rowEl);
                }
                else {
                    $children.eq(index).before($rowEl);
                }
            }
        },

        refresh: function () {
            for (var i = 0; i < this.rows.length; i++) {
                this.rows[i].remove();
            }

            this.rows = this.collection.map(function (model) {
                var rowClass = model instanceof EmptyModel ? Grid.EmptyModelRow : this.row;
                var row = new rowClass({
                    columns: this.columns,
                    model: model
                });

                return row;
            }, this);
            this._unshiftEmptyRowMayBe();

            this.render();

            this.collection.trigger("backgrid:refresh", this);

            return this;
        },

        /**
         Moves focus to the next renderable and editable cell and return the
         currently editing cell to display mode.

         @param {Backbone.Model} model The originating model
         @param {Backgrid.Column} column The originating model column
         @param {Backgrid.Command} command The Command object constructed from a DOM
         Event
         */
        moveToNextCell: function (model, column, command) {
            var i = this.collection.indexOf(model);
            var j = this.columns.indexOf(column);

            if (model.isNew()) {
                if (command.cancel()) {
                    this.collection.remove(model);
                } else if (command.save()) {
                    // same as Cell.applyEditMode
                    model.trigger('backgrid:beforeapply', model);
                    model.trigger('backgrid:apply', model);
                }
                return ;
            }

            this.rows[i].cells[j].exitEditMode();

            if (command.moveUp() || command.moveDown() || command.moveLeft() ||
                command.moveRight() || command.save()) {
                var l = this.columns.length;
                var maxOffset = l * this.collection.length;

                if (command.moveUp() || command.moveDown()) {
                    var row = this.rows[i + (command.moveUp() ? -1 : 1)];
                    if (row) row.cells[j].enterEditMode();
                }
                else if (command.moveLeft() || command.moveRight()) {
                    var right = command.moveRight();
                    for (var offset = i * l + j + (right ? 1 : -1);
                         offset >= 0 && offset < maxOffset;
                         right ? offset++ : offset--) {
                        var m = ~~(offset / l);
                        var n = offset - m * l;
                        var cell = this.rows[m].cells[n];
                        if (cell.column.get("renderable") && cell.column.get("editable")) {
                            cell.enterEditMode();
                            break;
                        }
                    }
                }
            }
        },

        _unshiftEmptyRowMayBe: function () {
            if (this.rows.length === 0) {
                this.rows.unshift(new Grid.EmptyRow({
                    emptyText: this.useEmptyText ? this.emptyText : this.loadingText,
                    columns: this.columns
                }));
                this.render();
            }
        },

        /**
         * Alters emptyText from 'Loading' to 'Not found'
         */
        updateEmptyText: function(){
            this.useEmptyText = true;
            if (this.rows.length == 1 && this.rows[0] instanceof Grid.EmptyRow) {
                this.rows[0].emptyText = this.emptyText;
                this.rows[0].render();
            }
        }

    });

    return Grid;

});