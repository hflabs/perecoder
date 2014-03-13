'use strict';

define([
    'underscore',
    'view/item-view',
    'text!template/common/footer.html'
], function (_, ItemView, viewTemplate) {

    /**
     * Application footer widget.
     * @class
     */
    var FooterView = ItemView.extend({
        template: _.template(viewTemplate),
        modelEvents: {
            'change:version': 'render'
        }
    });

    return FooterView;

});
