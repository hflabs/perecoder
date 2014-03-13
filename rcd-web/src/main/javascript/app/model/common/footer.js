'use strict';

define(function(require) {
    var Backbone = require('backbone'),
        Version = require('model/common/version');

    /**
     * Application footer data model.
     * @type {*}
     */
    var Footer = Backbone.Model.extend({
        defaults: {
            copyright: '2005-2014 © HFLabs',
            licenseUrl: 'http://www.hflabs.ru'
        },
        initialize: function(){
            var self = this,
                version = new Version();
            this.set('version', version);
            version.fetch()
                .done(function(){
                    self.trigger('change:version');
                });
        }
    });

    return Footer;
});