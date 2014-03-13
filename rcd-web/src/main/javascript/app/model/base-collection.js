'use strict';

define([
    'backbone',
    'model/syncable'
], function (Backbone, Syncable) {

    /**
     * Base collection.
     * @class
     */
    return Backbone.Collection.extend(Syncable);

});
