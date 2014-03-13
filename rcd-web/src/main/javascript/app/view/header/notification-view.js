'use strict';

define(function(require) {
    var _ = require('underscore'),
            ViewHelpers = require('view/view-helpers'),
            ItemView = require('view/item-view'),
            viewTemplate = require('text!template/header/notification.html');

    /**
     * Individual notification widget.
     * @class
     */
    var NotificationView = ItemView.extend({
        tagName: 'li',
        template: _.template( viewTemplate ),
        events: {
            'click .close': 'markAsProcessed'
        },
        modelEvents: {
            'change': 'render'
        },

        templateHelpers: function(){
            var model = this.model;
            return _.extend({
                    getIconClass: function(){
                        return {
                            //ERROR: 'ERROR', // default
                            NO_GROUP: 'no-group',
                            NO_DICTIONARY: 'no-dictionary',
                            //NO_VALUE: 'NO_VALUE',
                            NO_RULE: 'no-rule',
                            //NO_RULE_ALIAS: 'NO_RULE_ALIAS',
                            NO_RULE_SET: 'no-ruleset'
                        }[this.type] || '';
                    },
                    recodeText: function() {
                        switch (this.type) {
                            case 'NO_RULE_ALIAS':
                                return '';
                                break;
                            default:
                                var fromDictionary = this.fromDictionary && this.fromDictionary.name,
                                    fromGroup = this.fromGroup && this.fromGroup.name,
                                    toDictionary = this.toDictionary && this.toDictionary.name,
                                    toGroup = this.toGroup && this.toGroup.name;
                                if (fromDictionary && fromGroup) {
                                    fromDictionary += ' [ ' + fromGroup + ' ]';
                                }
                                if (toDictionary && toGroup) {
                                    toDictionary += ' [ ' + toGroup + ' ]';
                                }
                                return _.compact([fromDictionary, toDictionary]).join(' > ');
                        }
                    },
                    recodeUrl: function() {
                        return model.recodeUrl();
                    },
                    recodeField: function() {
                        switch (model.get('type')) {
                            case 'NO_RULE_ALIAS':
                                return model.get('ruleSetName');
                            case 'NO_VALUE':
                                return model.get('fromValue');
                        }
                        return '';
                    },
                    recodeDates: function(){
                        var start, end;
                        if (!this.endDate || this.startDate.valueOf() == this.endDate.valueOf()) {
                            start = this.startDate.toString();
                        } else {
                            if (this.startDate.dateEquals(this.endDate)) {
                                start = this.startDate.toString();
                                end = this.endDate.toTimeString();
                            } else {
                                start = this.startDate.toDateString();
                                end = this.endDate.toDateString();
                            }
                        }
                        return _.compact([start, end]).join(' &ndash; ');
                    }
                },
                ViewHelpers.templateHelpers
            );
        },

        /**
         * Marks notification as processed.
         */
        markAsProcessed: function() {
            this.model.markAsProcessed();
        },

        /**
         * @override
         */
        close: function(){
            var self = this,
                args = arguments;
            this.undelegateEvents();
            this.$el.fadeOut(function(){
                $(this).dequeue();
                ItemView.prototype.close.apply(self, args);
            });
        }

    });

    return NotificationView;

});
