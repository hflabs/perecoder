"use strict";
define(function(require){

    var _ = require('underscore'),
        Backbone = require('backbone'),
        Backgrid = require('backgrid'),
        Grid = require('grid/base-grid');

    /**
     * Completely rewritten
     * [Backgrid.Extension.Paginator]{@link http://backgridjs.com/api/index.html#!/api/Backgrid.Extension.Paginator}
     * @class
     */
    Grid.Paginator = Backbone.View.extend({

        /** @property */
        className: "paginator",

        /** @property */
        windowSize: 10,

        /**
         @property {Object} fastForwardHandleLabels You can disable specific
         handles by setting its value to `null`.
         */
        fastForwardHandleLabels: {
            //first: '<i class=""></i>',
            prev: '<i class="icon-20 prev compact"></i>',
            next: '<i class="icon-20 next compact"></i>'
            //last: '<i class=""></i>'
        },

        /** @property */
        template: _.template('<ul><% _.each(handles, function (handle) { ' +
            '%><li <% if (handle.className) { %>class="<%= handle.className %>"<% } %> data-page="<%= handle.page %>"><%= handle.label %></li><% ' +
            '}); %></ul>'),

        /** @property */
        events: {
            "click li": "changePage"
        },

        /**
         Initializer.

         @param {Object} options
         @param {Backbone.Collection} options.collection
         */
        initialize: function (options) {
            Backgrid.requireOptions(options, ["collection"]);

            if (options && options.windowSize) {
                this.windowSize = options.windowSize;
            }

            var collection = this.collection;
            var fullCollection = collection.fullCollection;
            if (fullCollection) {
                this.listenTo(fullCollection, "add", this.render);
                this.listenTo(fullCollection, "remove", this.render);
                this.listenTo(fullCollection, "reset", this.render);
            }
            else {
                this.listenTo(collection, "add", this.render);
                this.listenTo(collection, "remove", this.render);
                this.listenTo(collection, "reset", this.render);
                this.listenTo(collection, "state", this.render);
            }
        },

        /**
         jQuery event handler for the page handlers. Goes to the right page upon
         clicking.

         @param {Event} e
         */
        changePage: function (e) {
            e.preventDefault();

            var $li = $(e.target);
            if (!$li.is('li')) {
                $li = $li.closest('li');
            }
            if (!$li.hasClass("active") && !$li.hasClass("disabled")) {

                var label = $li.attr('data-page');

                var collection = this.collection;

                switch (label) {
                    case 'first':
                        collection.getFirstPage();
                        return;
                    case 'prev':
                        collection.getPreviousPage();
                        return;
                    case 'next':
                        collection.getNextPage();
                        return;
                    case 'last':
                        collection.getLastPage();
                        return;
                    case '...':
                        return;
                }

                var state = collection.state;
                var pageIndex = +label;
                collection.getPage(state.firstPage === 0 ? pageIndex - 1 : pageIndex);
            }
        },

        /**
         Internal method to create a list of page handle objects for the template
         to render them.

         @return {Array.<Object>} an array of page handle objects hashes
         */
        makeHandles: function () {

            var handles = [];
            var collection = this.collection;
            var state = collection.state;

            var firstPage = state.firstPage;
            var lastPage = +state.lastPage;
            var currentPage = Math.max(state.currentPage, state.firstPage);

            var windowStart = currentPage - Math.round(this.windowSize / 2);
            var windowEnd = currentPage + Math.round(this.windowSize / 2);

            var overflow = windowEnd - lastPage;
            if (overflow > 0) {
                windowStart -= overflow;
                windowEnd -= overflow;
            }
            overflow = firstPage - windowStart;
            if (overflow > 0) {
                windowStart += overflow;
                windowEnd += overflow;
            }

            windowStart = Math.max(firstPage, windowStart);
            windowEnd = Math.min(lastPage, windowEnd);

            if (collection.mode !== "infinite") {
                for (var i = windowStart; i <= windowEnd; i++) {
                    var page = firstPage ? i : i+1;
                    handles.push({
                        label: page,
                        //title:
                        page: page,
                        className: currentPage === page ? "active" : undefined
                    });
                }
            }

            if (windowStart > firstPage) {
                handles[0] = { label: firstPage, page: firstPage };
                handles[1] = { label: '...', page: '...' };
            }

            if (windowEnd < lastPage) {
                handles[handles.length -1] = { label: lastPage, page: lastPage };
                handles[handles.length -2] = { label: '...', page: '...' };
            }

            var ffLabels = this.fastForwardHandleLabels;
            if (ffLabels) {

                if (ffLabels.prev) {
                    handles.unshift({
                        label: ffLabels.prev,
                        page: 'prev',
                        className: collection.hasPrevious() ? void 0 : "disabled"
                    });
                }

                if (ffLabels.first) {
                    handles.unshift({
                        label: ffLabels.first,
                        page: 'first',
                        className: collection.hasPrevious() ? void 0 : "disabled"
                    });
                }

                if (ffLabels.next) {
                    handles.push({
                        label: ffLabels.next,
                        page: 'next',
                        className: collection.hasNext() ? void 0 : "disabled"
                    });
                }

                if (ffLabels.last) {
                    handles.push({
                        label: ffLabels.last,
                        page: 'last',
                        className: collection.hasNext() ? void 0 : "disabled"
                    });
                }
            }

            return handles;
        },

        /**
         Render the paginator handles inside an unordered list.
         */
        render: function () {
            this.$el.empty();

            if (this.collection.state.lastPage > this.collection.state.firstPage) {
                this.$el.append(this.template({
                    handles: this.makeHandles()
                }));

                this.delegateEvents();
            }

            return this;
        }

    });

    return Grid;

});
