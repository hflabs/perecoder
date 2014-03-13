'use strict';

define([
    'underscore',
    'marionette',
    'routes'
], function (_, Marionette, routes) {

    /**
     * Application router.
     * @class
     */
    var AppRouter = Marionette.AppRouter.extend({

        /**
         * Initialize all routes, directly navigable by user.
         */
        initAppRoutes: function() {
            this.appRoute(routes.GROUPS_APPROUTE, 'showGroups');
            this.appRoute(routes.DICTIONARIES_APPROUTE, 'showDictionaries');
            this.appRoute(routes.RECORDS_APPROUTE, 'showRecords');
            this.appRoute(routes.RECODES_APPROUTE, 'showRecodes');
            this.appRoute(routes.TASKS_APPROUTE, 'showTasks');
        }

    });

    return _.extend(AppRouter, routes);

});
