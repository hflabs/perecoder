"use strict";
define(function(require){

    var Backgrid = require('backgrid-select-all');
    var Grid = require('grid/base-grid');

    /**
     * Completely rewritten
     * [Backgrid.Extension.SelectAllHeaderCell]{@link http://backgridjs.com/api/index.html#!/api/Backgrid.Extension.SelectAllHeaderCell}
     * @class
     */
    Grid.SelectAllHeaderCell = Backgrid.Extension.SelectAllHeaderCell.extend({

        /**
         * @override
         */
        initialize: function(options) {
            Backgrid.requireOptions(options, ["column", "collection"]);

            this.column = options.column;
            if (!(this.column instanceof Backgrid.Column)) {
                this.column = new Backgrid.Column(this.column);
            }

            var collection = this.collection;
            var selectedModels = this.selectedModels = {};

            this.listenTo(collection, "backgrid:selected", function (model, selected) {
                if (selected) {
                    selectedModels[model.id || model.cid] = model;
                    if (_.keys(selectedModels).length == collection.length) {
                        this.$el.find(":checkbox").prop("checked", true);
                    }
                }
                else {
                    delete selectedModels[model.id || model.cid];
                    this.$el.find(":checkbox").prop("checked", false);
                }
            });

            this.listenTo(collection, 'remove', function (model) {
                delete selectedModels[model.id || model.cid];
            });

            this.listenTo(collection, "backgrid:refresh", function () {
                this.$el.find(":checkbox").prop("checked", false);
                for (var i = 0; i < collection.length; i++) {
                    var model = collection.at(i);
                    if (selectedModels[model.id || model.cid]) {
                        model.trigger('backgrid:select', model, true);
                    }
                }
            });
        }

    });

    return Grid;
});
