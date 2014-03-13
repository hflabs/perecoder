'use strict';

define(function(require){
    var _ = require('underscore'),
        ItemView = require('view/item-view'),
        viewTemplate = require('text!template/recodes/rrs-aside-new-dictionary.html');

    var RRSAsideNewDictionaryView = ItemView.extend({
        tagName: 'li',
        template: _.template(viewTemplate),
        events: {
            'click a:not([disabled])': 'onClick'
        },
        onClick: function(e){
            this.model.trigger('rrs:create', this.model, $(e.target));
        }

    });

    return RRSAsideNewDictionaryView;
});