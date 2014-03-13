'use strict';

define([
    'underscore',
    'view/item-view',
    'text!template/sync/sync-popup-errors-view.html'
], function (_, ItemView, Template) {

    var SyncPopupErrorsView = ItemView.extend({
        className: 'modal fade in',
        attributes: {style: 'display:none'},
        template: _.template(Template),

        onShow: function(){
            this.$el.modal('show');
        },
        onClose: function(){
            this.$el.modal('hide');
        }
    });

    return SyncPopupErrorsView;
});
