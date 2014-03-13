'use strict';
define(function(require){

    var _ = require('underscore'),
        GroupsList = require('model/groups/groups-list');

    /**
     * Modified GroupsList, that fetches only non-empty groups
     * @class
     */
    var RRSAsideNewGroupsList = GroupsList.extend({

        parse: function(resp){
            var currentDictionaryId = this.currentDictionaryId;
            var currentGroupId = this.currentGroupId;
            var models = _.compact(_.map(resp, function(item){
                if (item.statistic && item.statistic.totalCount && (currentGroupId != item.id || item.statistic.totalCount > 1)) {
                    item.currentDictionaryId = currentDictionaryId;
                    return item;
                }
            }));
            return models;
        }
    });

    return RRSAsideNewGroupsList;
});