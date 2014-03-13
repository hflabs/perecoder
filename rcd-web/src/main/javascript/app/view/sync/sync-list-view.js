'use strict';

define(function(require){
    
    var _ = require('underscore'),
        i18n = require('locale/i18n'),
        ListView = require('view/list-view'),
        SyncSourceView = require('view/sync/sync-source-view'),
        SyncEmptyView = require('view/sync/sync-empty-view'),
        viewTemplate = require('text!template/sync/sync-list.html');
        
    /**
     * Widget with list of all sync sources.
     * @class
     */
    var SyncListView = ListView.extend({
        template: _.template(viewTemplate),
        itemView: SyncSourceView,
        itemViewContainer: 'table',
        emptyView: SyncEmptyView,
        emptyMessage: i18n.tc('view.common.loading'),
        initialize: function(){
            this.listenToOnce(this.collection, 'sync', this.updateEmptyText);
        },
        updateEmptyText: function(){
            this.emptyMessage = i18n.tc('view.sync.empty');
            if (this.collection.length == 0) {
                this.render();
            }
        }
    });

    return SyncListView;

});
