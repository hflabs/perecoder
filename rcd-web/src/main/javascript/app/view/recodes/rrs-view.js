'use strict';

define([
    'underscore',
    'view/item-view',
    'text!template/recodes/rrs.html'
], function (_, ItemView, viewTemplate) {

    /**
     * Individual recode-rule-set item.
     * @class
     */
    var RRSView = ItemView.extend({
        tagName: 'li',
        template: _.template(viewTemplate),
        modelEvents: {
            'change': 'render'
        }
    });

    return RRSView;

});
