'use strict';

define(function(require){
    var Marionette = require('marionette'),
        RRSView = require('view/recodes/rrs-view');

    /**
     * List of recode-rule-sets.
     * @class
     */
    var RRSListView = Marionette.CollectionView.extend({
        tagName: 'ul',
        className: 'recodes',
        itemView: RRSView,
        collectionEvents: {
            'sort': 'render'
        }
    });

    return RRSListView;

});
