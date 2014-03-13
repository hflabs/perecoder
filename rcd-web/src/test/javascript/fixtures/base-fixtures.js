define(['underscore','jasmine_jquery','sinon'], function(_, $) {

    $.fn.modal = function(action){
        return this.each(function(){
            $(this)[action]();
        });
    };

    var fixtures = {
        headers: {
            'Content-Type': 'application/json'
        },
        responses: [],
        createServer: function () {
            var server = sinon.fakeServer.create();
            _.each(fixtures.responses, function (resp) {
                server.respondWith.apply(server, resp);
            });
            return server;
        },
        fakeRegion: {
            show: function(view) {
                view.render();

                $(sandbox()).appendTo($('body'))
                    .append(view.el);

                view.triggerMethod('show');
            }
        }
    };

    return fixtures;
});