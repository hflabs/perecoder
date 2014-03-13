'use strict';

define(function(require){
    var _ = require('underscore'),
        i18n = require('locale/i18n'),
        fixtures = require('fixtures'),
        routes = require('routes'),
        NotificationList = require('model/header/notification-list'),
        NotificationsView = require('view/header/notification-list-view');

    describe('Notifications list', function(){

        beforeEach(function(){
            this.server = fixtures.createServer();

            this.notifications = new NotificationList();
            this.view = new NotificationsView({ collection: this.notifications });
            this.view.render();

            this.notifications.fetch();
            this.server.respond();

        });

        afterEach(function(){
            this.server.restore();
        });

        it('has all notifications from server displayed', function(){
            var notificationsLength = fixtures.notifications.data.length;
            expect(this.notifications.length).toEqual(notificationsLength);
            expect(this.view.$('.items li').length).toEqual(notificationsLength);
        });

        it('correctly displays every notification type', function(){
            var view = this.view;
            _.each(fixtures.notifications.data, function(notification, idx) {
                    var $notification = view.$('.items li').eq(idx);
                    expect($notification.find('small')).toContainText(i18n.tc('model.notification.' + notification.type));
                    expect($notification.find('small')).toContainText(notification.count);
                }
            );

        });

    });

});
