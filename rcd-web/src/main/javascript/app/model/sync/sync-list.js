'use strict';

define([
    'model/base-collection',
    'model/sync/sync-source'
], function (BaseCollection, SyncSource) {

    /**
     * List of sync sources.
     * @class
     */
    var SyncList = BaseCollection.extend({
        model: SyncSource,
    });

    return SyncList;
});
