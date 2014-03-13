'use strict';

define([
    'marionette',
    'view/view-helpers'
], function (Marionette, ViewHelpers) {

    /**
     * Item view with overloaded model serialization.
     * @class
     */
    var ItemView = Marionette.ItemView.extend({

        /**
         * @override
         */
        serializeData: function(){
            var data = {};
            if (this.model) {
              data = this.model.attributes;
            }
            else if (this.collection) {
              data = { items: this.collection.attributes };
            }
            return data;
          }

    });

    return ItemView.extend(ViewHelpers);

});
