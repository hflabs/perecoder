'use strict';

define([
    'underscore',
    'view/item-view',
    'view/view-helpers',
    'text!template/recodes/rrs-target.html'
], function (_, ItemView, ViewHeplers, viewTemplate) {

    /**
     * Individual recode-rule-set item.
     * @class
     */
    var RRSTargetView = ItemView.extend({
        tagName: 'li',
        template: _.template(viewTemplate),
        templateHelpers: function(){
            var self = this;
            return _.extend({
                isCurrent: function(){
                    return self.isModelCurrent();
                }
            }, ViewHeplers.templateHelpers);
        },
        initialize: function(){
            this.$el.toggleClass('current', this.isModelCurrent());
        },
        isModelCurrent: function(){
            return this.model.get('toDictionary').id == this.model.collection.currentId;
        }
    });

    return RRSTargetView;

});
