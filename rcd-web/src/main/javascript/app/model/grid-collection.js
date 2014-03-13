'use strict';

define(function (require) {
    var _ = require('underscore'),
        Backbone = require('backbone'),
        PageableCollection = require('backbone-pageable');

    Backbone.PageableCollection = PageableCollection;

    var DEFAULT_STATE = {
            firstPage: 1,
            currentPage: 1,
            pageSize: 10
        },
        DEFAULT_QUERY_PARAMS = {
            pageSize: 'pageSize',
            currentPage: 'page',
            sortKey: 'sortOrderKey',
            order: 'sortOrderValue',
            directions: {
                "-1": "ASCENDING",
                "1": "DESCENDING"
            }
        };

    /**
     * Pageable collection with predefined request param names
     * Additional options:
     *  forbidden {Array} Models with id from this array will not be created
     *
     * @class
     */
    var GridCollection = PageableCollection.extend({
        state: DEFAULT_STATE,
        queryParams: DEFAULT_QUERY_PARAMS,

        initialize: function(models, options){
            PageableCollection.prototype.initialize.apply(this,arguments);
            this.forbidden = options && options.forbidden;
            this.listenTo(this, 'filter', this.onFilter);
        },

        parse: function (resp) {
            var stateKeys = ['currentPage', 'lastPage'],
                newState = _.pick(resp, stateKeys),
                oldState = _.pick(this.state, stateKeys);

            if (!_.isEqual(oldState, newState)) {
                _.extend(this.state, newState);
                this.trigger('state');
            }

            var models = resp.content;
            if (this.forbidden) {
                models = _.reject(models, function(model){
                    return this.forbidden.indexOf(model.id) >= 0;
                }, this);
            }
            return models;
        },

        fetch: function(options){
            (options || (options = {})).reset = true;
            return PageableCollection.prototype.fetch.call(this, options);
        },

        onFilter: function(pattern){
            if (this.queryParams.search != pattern) {
                if (pattern) {
                    this.queryParams.search = pattern;
                } else {
                    delete this.queryParams.search;
                }
                this.fetch();
            }
        },

        resetState: function(state){
            this.state = _.extend({}, DEFAULT_STATE, state);
        },

        resetQueryParams: function(){
            delete this.queryParams.search;
        }

    });

    return GridCollection;
});