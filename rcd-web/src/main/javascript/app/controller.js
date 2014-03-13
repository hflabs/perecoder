'use strict';

define(function(require) {
    var _ = require('underscore'),
        $ = require('jquery'),
        Backbone = require('backbone'),
        Marionette = require('marionette'),
        util = require('util'),
        routes = require('routes'),
        app = require('app'),
        AppSync = require('app-sync'),
        i18n = require('locale/i18n'),
        log = require('log'),
        ListAsideController = require('controller/list-aside-controller'),
        NewAsideController = require('controller/new-aside-controller'),
        RecodesAsideController = require('controller/recodes-aside-controller'),
        GroupsController = require('controller/groups-controller'),
        DictionariesController = require('controller/dictionaries-controller'),
        ContentController = require('controller/content-controller'),
        RecodesController = require('controller/recodes-controller'),
        SyncController = require('controller/sync-controller'),
        HeaderController = require('controller/header-controller'),
        Footer = require('model/common/footer'),
        FooterView = require('view/common/footer-view');

    /**
     * Envelope for standard Backbone.sync
     *
     * Supports custom methods. Default HTTP method for them is 'PUT', can be overriden in options
     * Also handles server errors
     */
    var _sync = AppSync(Backbone.sync, app.vent);

    /**
     * Apply _sync for all syncable classes
     */
    _.each([Backbone.Model, Backbone.Collection, Marionette.Controller], function(cls){
        cls.prototype.sync = _sync;
    });

    /**
     * Minimal time (in milliseconds) to keep a message about long ajax request visible
     * @type {number}
     */
    var HANG_MESSAGE_MIN_DURATION = 2000;
    
    /**
     * Application controller. Handles top-level view management.
     * @class
     */
    var AppController = Marionette.Controller.extend({

        initialize: function() {
            this.pageControllers = [
                this.groupsPageController = new GroupsController(),
                this.dictionariesPageController = new DictionariesController(),
                this.contentPageController = new ContentController(),
                this.recodesPageController = new RecodesController(),
                this.syncPageController = new SyncController(),
                this.listAsideController = new ListAsideController(),
                this.newAsideController = new NewAsideController(),
                this.recodesAsideController = new RecodesAsideController()
            ];
            this.currentPageController = null;
            this.currentAsideController = null;
            this.previousAsideController = null;
            this.currentAsideMode = null;
            this.previousAsideMode = null;

            this.headerController = null;
            this.footer = null;
            this.footerView = null;

            this.$root = $('body');

            _.each(this.pageControllers, this.bindControllerEvents, this);

            this.listenTo(app.vent, {
                'error': this.onError,
                'invalid': this.onInvalid,
                'hang': this.onHang,
                'navigate': this.navigate,
                'aside:new': this.onAsideNew,
                'aside:previous': this.onAsidePrevious
            });

            // Disable logging in testing environment
            if (typeof console != 'undefined') {
                log.setLevel(log.levels.DEBUG);
            }
        },

        start: function() {
            this.createHeader();
            this.createFooter();
            this.bindLayoutAnimation();
            this.enableGlobalNavigation();
        },

        createHeader: function() {
            this.headerController = new HeaderController();
            this.bindControllerEvents(this.headerController);
            this.headerController.open(app.header);
        },

        createFooter: function() {
            this.footer = new Footer;
            this.footerView = new FooterView({ model: this.footer });
            app.footer.show(this.footerView);
        },

        showGroups: function() {
            this.headerController.highlight('groups');
            this.setBodyAttrs({'data-page': 'groups'});
            this.switchPageController(this.groupsPageController);
            this.switchAsideController();
        },

        showDictionaries: function() {
            this.headerController.highlight('dictionaries');
            this.setBodyAttrs({'data-page': 'dictionaries'});
            this.switchPageController(this.dictionariesPageController, [arguments[2]]);
            this.switchAsideController();
        },

        showRecords: function() {
            var params = [arguments[1]];
            this.headerController.highlight('dictionaries');
            this.setBodyAttrs({'data-page': 'records'});
            this.switchPageController(this.contentPageController, params);
            this.switchAsideController(this.listAsideController, 'list', params);
        },

        showRecodes: function() {
            var params = [arguments[1]];
            this.headerController.highlight('dictionaries');
            this.setBodyAttrs({'data-page': 'recodes'});
            this.switchPageController(this.recodesPageController, params);
            this.switchAsideController(this.recodesAsideController, 'recodes', params);
        },

        showTasks: function() {
            this.headerController.highlight('tasks');
            this.setBodyAttrs({'data-page': 'tasks'});
            this.switchPageController(this.syncPageController);
            this.switchAsideController();
        },

        switchAsideController: function(controller, asideMode, params){
            if (this.currentAsideController == controller
                && controller && _.isEqual(params, controller.params)) {
                this.currentAsideController && this.currentAsideController.refresh();
                return;
            }
            this.previousAsideController = this.currentAsideController;
            this.previousAsideMode = this.currentAsideMode;
            this.setBodyAttrs({'data-aside': this.currentAsideMode = (asideMode || '')});
            this.currentAsideController && this.currentAsideController.close();
            this.currentAsideController = controller;
            if (this.currentAsideController) {
                this.currentAsideController.open(app.aside, params);
                this.currentAsideController.params = params;
            } else {
                app.aside.close();
            }
        },

        /**
         * Changes controller
         * @param ctrl  controller to open
         * @param {Array} params   parameters extracted from URL
         */
        switchPageController: function(controller, params) {
            if (controller == this.currentPageController
                && _.isEqual(params, controller.params)
            ) {
                this.currentPageController.refresh();
                return;
            }
            this.currentPageController && this.currentPageController.close();
            controller.params = params;
            this.currentPageController = controller;
            this.currentPageController.open(app.main, params);
        },

        /**
         * Connects controller to common message bus, which is <code>app.vent</code>
         *
         * Proxies all controller's events to <code>app.vent</code>
         * and subscribes controller on events mentioned in its <code>events</code> collection
         * @param controller
         */
        bindControllerEvents: function(controller){
            app.vent.listenTo(controller, 'all', function(){
                var args = _.toArray(arguments);
                if (args[0]) {
                    app.vent.trigger.apply(app.vent, args);
                    log.debug(controller.controllerName + ' fired ' + args[0], args );
                }
            });
            _.each(controller.events, function(methodName, event){
                var method = controller[methodName];
                if (_.isFunction(method)) {
                    controller.listenTo(app.vent, event, method);
                }
            });
        },

        /**
         * Navigates to specified page.
         * @param {String} url
         */
        navigate: function(url) {
            url = routes.relativeUrl(url);
            app.router.navigate(url, { trigger: true });
        },

        /**
         * Handles all server errors
         * @param model
         * @param xhr
         * @param options
         */
        onError: function(model,xhr,options){
            var error;
            try {
                error = JSON.parse(xhr.responseText) || {};
            } catch (ex) {
                error = {
                    globalErrors: [xhr.statusText]
                }
            }

            switch(xhr.status) {
                case 401:
                    // Unauthorised
                    window.location.reload(true);
                    return;
                case 404: case 0:
                    // Not found (not ready)
                    error.globalErrors = [i18n.tc('view.error.404')];
                    break;
                case 410:
                    // Object deleted
                    if (model && model.collection)
                        model.collection.remove(model);
                    break;
            }

            this.proceedErrors(error, options);
        },
        onInvalid: function(model, error, options){
            this.proceedErrors(error, options);
        },
        /**
         * Passes errors to displaying function
         * @param error Object {globalErrors: [<message>, ...], fieldErrors: {<fieldName>: '<message>', ...}}
         * @param options
         */
        proceedErrors: function(error, options){
            if (error.globalErrors && error.globalErrors.length) {
                if (_.isFunction(options && options.globalErrorsHandler)) {
                    options.globalErrorsHandler(error);
                } else {
                    _.each(error.globalErrors, function(msg){
                        this.headerController.addErrorAlert(msg);
                    }, this);
                }
            }
            if (error.fieldErrors && !_.isEmpty(error.fieldErrors)) {
                if (_.isFunction(options && options.fieldErrorsHandler)) {
                    options.fieldErrorsHandler(error);
                } else {
                    this.headerController.addErrorAlert(_.values(error.fieldErrors).join(', '));
                }
            }
        },
        onHang: function(xhr){
            var message = this.headerController.addInfoAlert(i18n.tc('model.common.hang'));
            if (message && xhr) {
                var self = this,
                    timer = $.Deferred(),
                    result = $.Deferred();
                setTimeout(function(){
                    timer.resolve();
                }, HANG_MESSAGE_MIN_DURATION);
                xhr.always(function(){
                    result.resolve();
                });
                $.when(result, timer).then(function(){
                    self.headerController.removeAlert(message);
                });
            }
        },

        setBodyAttrs: function(attrs){
            attrs = _.extend({
                'data-locale': i18n.options.locale_data.messages[''].lang
            }, attrs);
            this.$root.removeClass('loading')
                .attr(attrs);
        },

        /**
         * Forces page elements to be rendered
         */
        bindLayoutAnimation: function(){
            var eventName = $.support.transition && $.support.transition.end;
            if (eventName) {
                $(app.aside.el).on(eventName, function(){
                    $(window).resize();
                });
            }
        },

        onAsideNew: function(dictionaryId){
            this.switchAsideController(this.newAsideController, 'new', [dictionaryId]);
        },

        onAsidePrevious: function(){
            this.switchAsideController(
                this.previousAsideController,
                this.previousAsideMode,
                this.previousAsideController && this.previousAsideController.params
            );
        },

        enableGlobalNavigation: function(){
            var self = this;
            this.$root.on('click', 'a[data-local="true"]', function(e){
                var $a = $(this),
                    href = $a.attr('href');
                if (href) {
                    e.preventDefault();
                    self.navigate(href);
                }
            });
        }

    });

    return AppController;

});
