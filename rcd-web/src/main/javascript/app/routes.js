'use strict';

define([
    'underscore'
], function (_) {

    /**
     * List of application routes.
     * @class
     */
    return new function() {
        var routes = this;
        this.BASE_PATH = '/rcd/admin/';
        this.DATA_POSTFIX = 'data/';
        this.MODELS_POSTFIX = 'model/';
        this.ROOT = '';

        this.NOTIFICATIONS = this.ROOT + 'notifications/';
        this.NOTIFICATIONS_DATA = this.NOTIFICATIONS + this.DATA_POSTFIX;

        this.GROUPS = this.ROOT + '';
        this.GROUPS_APPROUTE = this.GROUPS;
        this.GROUPS_DATA = this.ROOT + 'groups/' + this.DATA_POSTFIX;
        this.GROUPS_MODELS = this.GROUPS_DATA + this.MODELS_POSTFIX;

        this.DICTIONARIES = this.ROOT + 'dictionaries/';
        this.DICTIONARIES_APPROUTE = /dictionaries(\/(([\w-]+)\/)?)?$/;
        this.DICTIONARIES_DATA = this.DICTIONARIES + this.DATA_POSTFIX;
        this.DICTIONARIES_MODELS = this.DICTIONARIES_DATA + this.MODELS_POSTFIX;
        this.DICTIONARIES_UPLOAD = this.DICTIONARIES_DATA + 'upload/';
        this.DICTIONARIES_DOWNLOAD = this.DICTIONARIES_DATA + 'download/';

        this.METAFIELDS_DATA = this.ROOT + 'metafields/' + this.DATA_POSTFIX;

        this.RECORDS = this.ROOT + 'records/';
        this.RECORDS_APPROUTE = new RegExp(this.RECORDS + '(([\\w-]+)\\\/)?$');
        this.RECORDS_DATA = this.RECORDS + this.DATA_POSTFIX;
        this.RECORDS_MODELS = this.RECORDS_DATA + this.MODELS_POSTFIX;

        this.RRS_DATA = this.ROOT + 'rrs/' + this.DATA_POSTFIX;

        this.RECODES = this.ROOT + 'recodes/';
        this.RECODES_APPROUTE = new RegExp(this.RECODES + '(([\\w-]+)\\\/)?$');
        this.RECODES_DATA = this.RECODES + this.DATA_POSTFIX;

        this.TASKS = this.ROOT + 'tasks/';
        this.TASKS_APPROUTE = new RegExp(this.TASKS + '?');
        this.TASKS_DATA = this.TASKS + this.DATA_POSTFIX;
        this.TASKS_MODELS = this.TASKS_DATA + this.MODELS_POSTFIX;

        this.LOGOUT = this.ROOT + 'logout/';
        this.VERSION = this.ROOT + 'version/';

        /**
         * Returns absolute url for specified relative url.
         * @param {string} url
         * @returns {string} absolute url
         */
        this.absoluteUrl = function(url) {
            if (url && url[0] === '/') {
                return url;
            }
            return routes.BASE_PATH + url;
        };

        /**
         * Builds full url to specified route
         * @param route
         * @param param
         */
        this.buildUrl = function(route, param){
            var url = route && this[route];
            if (_.isString(url) && param) {
                url += param + '/';
            }
            return this.absoluteUrl(url);
        };

        this.extractParam = function(url){
            var r = /(\/|#)(\w+)\/(([\w-]+)\/)?$/i.exec(url);
            return r && r[4];
        };

        /**
         * Returns url without the application base path.
         * @param {string} url
         * @returns {string} relative url
         */
        this.relativeUrl = function(url) {
            return url.replace(routes.BASE_PATH, '');
        };

    };

});
