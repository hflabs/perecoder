'use strict';

define(function(require) {
    var Backbone = require('backbone'),
        routes = require('routes'),
        GroupModel = require('model/groups/group-model');

    var GroupsList = Backbone.Collection.extend({
        model: GroupModel,
        url: routes.absoluteUrl(routes.GROUPS_DATA),

        fetch: function(options){
            if (this.search) {
                (options || (options = {})).data = {
                    search: this.search
                };
            }
            return Backbone.Collection.prototype.fetch.call(this, options);
        }
    });

    return GroupsList;
})