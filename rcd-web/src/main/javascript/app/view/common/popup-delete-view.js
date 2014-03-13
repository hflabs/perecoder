'use strict';

define([
    'jquery',
    'underscore',
    'view/item-view',
    'view/view-helpers',
    'text!template/common/popup-delete-view.html'
], function ($, _, ItemView, ViewHelpers, Template) {

    var PopupDeleteView = ItemView.extend({
        className: 'modal fade in',
        attributes: {style: 'display:none'},
        template: _.template(Template),
        templateHelpers: function(){
            var self = this;
            return _.extend({
                getConfirm: function(){
                    return self.confirm;
                }
            }, ViewHelpers.templateHelpers);
        },
        events: {
            'click [data-action="submit"]': 'destroyModel',
            'keydown': 'onKeydown'
        },
        initialize: function(options){
            this.confirm = options.confirm;
        },
        onShow: function(){
            this.$el.modal('show');
        },
        onClose: function(){
            this.$el.modal('hide');
        },
        destroyModel: function(){
            this.model.destroy({ wait: true });
        },
        onKeydown: function(e){
            switch (e.keyCode) {
                case 9:
                    e.preventDefault();
                    this.$('.modal-footer button:not(:focus)').focus();
            }
        }
    });

    return PopupDeleteView;
});
