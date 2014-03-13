'use strict';

define(function(require){
    var _ = require('underscore'),
        i18n = require('locale/i18n'),
        Message = require('model/common/message'),
        ItemView = require('view/item-view'),
        viewTemplate = require('text!template/groups/group-empty.html');

    /**
     * Individual group widget.
     * @class
     */
    var GroupEmptyView = ItemView.extend({
        template: _.template(viewTemplate),
        emptyText: i18n.tc('view.common.loading'),
        initialize: function(){
            this.model = new Message({
                text: this.emptyText,
                type: Message.MessageTypes.EMPTY
            });
        }
    });

    return GroupEmptyView;

});
