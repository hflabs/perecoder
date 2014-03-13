'use strict';

define(function(require) {
    var _ = require('underscore'),
        ItemView = require('view/item-view'),
        viewTemplate = require('text!template/header/alert.html');

    /**
     * Individual run-time alert item.
     * @class
     */
    var AlertView = ItemView.extend({
        tagName: 'li',
        template: _.template( viewTemplate )
    });

    return AlertView;

});
