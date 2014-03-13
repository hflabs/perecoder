'use strict';

define([
    'backbone',
    'model/syncable'
], function (Backbone, Syncable) {

    /**
     * Base model.
     * @class
     */
    return Backbone.Model.extend({
        isWritable: function(){
            var writable = this.get('writable');
            return _.isUndefined(writable) || writable === true;
        },

        isReadonly: function(){
            return !this.isWritable();
        },

        /**
         * Sets attribute value, invoking client-side validation and saving to server.
         *
         * If either validation or saving fails, previous value is restored.
         * @param {String} attr Name of attribute
         * @param {String} value         New value
         * @param options       Optional parameters passed to 'save'
         * @returns xhr or <code>false</code>
         */
        safeSet: function(attr, value, options){
            var oldValue = this.get(attr);
            if (this.set(attr, value, {
                // suppress 'change' events to avoid re-rendering
                silent: true,
                validate: true
            })
                // unsaved models are just validated
                && !this.isNew()
            ) {
                var self = this,
                    xhr = this.save(null, options);
                if (xhr) {
                    xhr.fail(function () {
                        self.set(attr, oldValue);
                        self.trigger('change', self);
                    });
                }
                return xhr;
            }
            return false;
        }

    }, Syncable);

});
