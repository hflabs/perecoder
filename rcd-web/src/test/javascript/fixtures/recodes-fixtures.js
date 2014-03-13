define(function(require){

    var _ = require('underscore'),
        fixtures = require('base-fixtures');

    var data = [
            {
                rrsId: 'rrs1',
                fromRecordId: 'record1',
                toRecordId: 'record3'
            },
            {
                rrsId: 'rrs1',
                fromRecordId: 'record2',
                toRecordId: 'record3'
            },
            {
                rrsId: 'rrs2',
                fromRecordId: 'record3',
                toRecordId: 'record1'
            }
        ],
        urls = {
            fetch: /recodes\/data\/([\w-]+)\/(\?recordIDs=([\w%-]+))(&|$)/,
            update: /recodes\/data\/([\w-]+)(\/$|\?|$)/
        },
        dataOfRRS = function(rrsId, recordIDs){
            return _.filter(data, function(recode){
                return recode.rrsId == rrsId &&
                    recordIDs.indexOf(recode.fromRecordId) >= 0;
            });
        };

    fixtures.recodes = {
        data: data,
        urls: urls,
        dataOfRRS: dataOfRRS
    };

    fixtures.responses.push(['GET', urls.fetch,
        function(xhr, rrsId, _ids, recordIDs){
            xhr.respond(200, fixtures.headers, JSON.stringify(
                dataOfRRS(rrsId, decodeURIComponent(recordIDs).split(','))
            ));
        }
    ]);
    fixtures.responses.push(['POST', urls.update,
        function (xhr) {
            var request = JSON.parse(xhr.requestBody),
                response = [];
            _.each(request.fromRecordIDs, function(fromRecordId){
                response.push({
                    fromRecordId: fromRecordId,
                    toRecordId: request.toRecordId
                });
            });
            xhr.respond(200, fixtures.headers, response);
        }
    ]);

    return fixtures;
});