'use strict';

define([
    'backbone'
], function (Backbone) {

    var MessageTypes = {
        INFO: 'info',
        EMPTY: 'empty',
        ERROR: 'error'
    };

    /**
     * Custom message.
     * @class
     */
    var Message = Backbone.Model.extend({
        defaults: {
            type: MessageTypes.INFO,
            text: ''
        }
    });

    Message.MessageTypes = MessageTypes;

    return Message;

});
