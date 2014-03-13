'use strict';

define(function (require) {
    var _ = require('underscore'),
        $ = require('jquery'),
        Marionette = require('marionette'),
        Grid = require('grid/grid'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        DictionaryModel = require('model/dictionaries/dictionary-model'),
        ViewHelpers = require('view/view-helpers'),
        viewTemplate = require('text!template/dictionaries/dictionaries-layout.html');

    var windowEventNS = '.page_dictionaries';
    var DELETE_CELL_WIDTH = 48;

    /**
     * Dictionaries layout.
     * Handles all the UI-only logic for dictionaries.
     * @class
     */
    var DictionariesLayout = Marionette.Layout.extend({
        template: _.template(viewTemplate),
        regions: {
            dictionariesRegion: '[data-region="dictionaries"]',
            pagerRegion: '[data-region="pager"]',
            popupsRegion: '[data-region="popups"]'
        },
        ui: {
            addBtn: '[data-action="add"]',
            deleteBtn: '[data-action="delete"]',
            filter: '[data-field="filter"]'
        },
        templateHelpers: function(){
            var self = this;
            return _.extend({
                getCurrentGroup: function(){
                    return self.currentGroup;
                }
            }, ViewHelpers.templateHelpers);
        },
        events: {
            'keypress [data-field="filter"]': 'filterKeyup',
            'click span.search': 'applyFilter'
        },
        triggers: {
            'click [data-action="add"]': 'dictionaries:add',
            'click [data-action="upload"]': 'dictionaries:upload',
            'click [data-action="delete"]': 'dictionaries:delete'
        },

        /* public */

        initialize: function (options) {
            this.list = options.list;
            this.groupsList = options.groupsList;
            this.currentGroup = options.currentGroup;
            this.createParts();
            this.listenTo(this.list, 'add remove reset', this.onListChanged);
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
            this.dictionariesRegion.show(this.grid);
            this.pagerRegion.show(this.paginator);
            this.pinElements();
        },
        onClose: function () {
            $(window).off(windowEventNS);
        },

        /* private */

        createParts: function () {
            this.grid = new Grid.Grid({
                attributes: {
                    'data-control': 'dictionaries-main-table'
                },
                collection: this.list,
                columns: this.extendColumns(),
                body: Grid.Body,
                emptyText: i18n.tc('view.dictionaries.empty')
            });
            this.paginator = new Grid.Paginator({
                attributes: {
                    'data-control': 'dictionaries-main-paginator'
                },
                collection: this.list
            });
        },

        /**
         * Extends columns info with modelParams
         * @return {Array}
         */
        extendColumns: function () {
            var columns = [
                {
                    name: '',
                    cell: Grid.DeleteRowCell,
                    headerCell: Grid.HeaderCell,
                    editable: true,
                    sortable: false,
                    width: DELETE_CELL_WIDTH
                },
                {
                    name: 'name',
                    cell: Grid.UriCell.extend({
                        hrefTemplate: '<%- buildUrl("RECORDS", id) %>',
                        iconClassName: 'dictionary'
                    }),
                    headerCell: Grid.HeaderCell,
                    label: i18n.tc('model.dictionaries.name'),
                    sortable: false
                },
                {
                    name: 'description',
                    cell: Grid.OptionalStringCell.extend({
                        iconClassName: 'page'
                    }),
                    headerCell: Grid.HeaderCell,
                    label: i18n.tc('model.dictionaries.description'),
                    sortable: false
                }
            ];
            if (!this.currentGroup) {
                columns.push({
                    name: 'groupId',
                    cell: Grid.StringCell,
                    headerCell: Grid.HeaderCell,
                    label: i18n.tc('model.dictionaries.groupId'),
                    editable: false,
                    sortable: false,
                    formatter: new Grid.GroupNameFormatter(this.groupsList)
                });
            }
            var fields = DictionaryModel.prototype.modelParams.fields || {};
            var columns = _.map(columns, function (column) {
                var field = fields[column.name];
                if (field) {
                    column.maxlength = field.maxLength;
                    column.sortable = !!field.sortable;
                }
                return column;
            }, this);

            return columns;
        },

        /**
         * Sets up elements for fixing at the window edge
         */
        pinElements: function () {
            var $pagerContainer = this.pagerRegion.$el,
                $paginator = this.paginator.$el,
                pagerHeight = $pagerContainer.height(),
                $window = $(window),
                $footer = $('footer');

            var scrollHandler = function(){
                var pagerPosition = $pagerContainer.position(),
                    footerPosition = $footer.position();
                if (pagerPosition && footerPosition) {
                    $paginator.toggleClass('pinned', pagerPosition.top + pagerHeight > footerPosition.top)
                        .css({
                            left: pagerPosition.left - $window.scrollLeft(),
                            width: $pagerContainer.width()
                        });
                }
            };

            $window.on('resize'+windowEventNS+' scroll'+windowEventNS, scrollHandler);
        },
        /**
         * Performs UI reaction on dictionaries adding/removing
         */
        onListChanged: function(){
            $(window).scroll();
        },
        filterKeyup: function(e){
            if (e && (e.keyCode || e.which) == 13) {
                this.applyFilter();
            }
        },
        applyFilter: function(){
            this.list.trigger('filter', this.ui.filter.val().trim());
        }
    });

    return DictionariesLayout;

});
