'use strict';

define(function(require){
    var Marionette = require('marionette'),
        i18n = require('locale/i18n'),
        GroupView = require('view/groups/group-view'),
        GroupEmptyView = require('view/groups/group-empty-view');

    /**
     * Individual group widget.
     * @class
     */
    var GroupsListView = Marionette.CollectionView.extend({
        className: 'tile-container',
        itemView: GroupView,
        emptyView: GroupEmptyView,
        initialize: function(){
            this.listenToOnce(this.collection, 'sync', this.updateEmptyText);
        },
        updateEmptyText: function(){
            GroupEmptyView.prototype.emptyText = i18n.tc('view.groups.empty');
            if (!this.collection.length) {
                this.render();
            }
        }
    });

    return GroupsListView;

});
