'use strict';

define(function(require) {
    var Marionette = require('marionette'),
        i18n = require('locale/i18n'),
        Message = require('model/common/message'),
        ViewHelpers = require('view/view-helpers');

    /**
     * Base list view (with optional message shown
     * if sync with server failed).
     * @class
     */
    var ListView = Marionette.CompositeView.extend({

        /**
         * @override
         */
        showEmptyView: function(){
            var EmptyView = Marionette.getOption(this, "emptyView");

            if (EmptyView && !this._showingEmptyView){
                this._showingEmptyView = true;
                var model = new Message({
                    type: Message.MessageTypes.EMPTY,
                    text: this.emptyMessage || i18n.t('model.common.no_data_found')
                });
                this.addItemView(model, EmptyView, 0);
            }
        }

    });

    return ListView.extend(ViewHelpers);
});
