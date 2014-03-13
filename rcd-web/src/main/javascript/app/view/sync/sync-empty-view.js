'use strict';

define([
    'underscore',
    'locale/i18n',
    'view/item-view',
    'text!template/sync/sync-source-error.html'
], function (_, i18n, ItemView, errorTemplate) {

    /**
     * Error message.
     * @class
     */
    var SyncEmptyView = ItemView.extend({
        tagName: 'tbody',
        template: _.template(errorTemplate)
    });

    return SyncEmptyView;

});
