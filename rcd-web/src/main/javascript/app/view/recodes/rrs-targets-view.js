'use strict';

define(function(require){
    var Marionette = require('marionette'),
        RRSTargetView = require('view/recodes/rrs-target-view');

    /**
     * List of recode-rule-sets.
     * @class
     */
    var RRSTargetsView = Marionette.CollectionView.extend({
        tagName: 'ul',
        itemView: RRSTargetView,
        collectionEvents: {
            'sort': 'render'
        }
    });

    return RRSTargetsView;

});
