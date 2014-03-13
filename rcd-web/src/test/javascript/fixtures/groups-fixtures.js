define(function(require) {

    var _ = require('underscore'),
        fixtures = require('base-fixtures');

    var data = [
            {
                "name": "group1",
                "id": "group1id",
                "owner": null,
                "description": "group1 description",
                "writable": true,
                "historyId": "466fac07-46e3-4591-86bf-1da2e8b5f21a",
                "statistic": {
                    "totalCount": 0,
                    "unmatchedCount": 0
                }
            },
            {
                "name": "group2",
                "id": "3ed65091-73d9-44fc-ba39-c586ec099ed2",
                "owner": null,
                "description": "group1 description",
                "writable": true,
                "historyId": "a5b224f8-68c8-41b2-bb07-a0fb18e46d95",
                "statistic": {
                    "totalCount": 1,
                    "unmatchedCount": 1
                }
            },
            {
                "name": "Default group",
                "id": "esnsi_1",
                "owner": "ESNSI",
                "description": null,
                "writable": false,
                "historyId": "4f3c141f-c500-4fee-b761-8d4c1bfedf62",
                "statistic": {
                    "totalCount": 2,
                    "unmatchedCount": 1
                }
            }
        ],
        urls = {
            fetchAll: /groups\/data\/(\?|$)/,
            fetchOne: /groups\/data\/([\w-]+)(\/$|\?|$)/,
            save: /groups\/data\/([\w-]+)(\/$|\?|$)/,
            create: /groups\/data\/(\?|$)/,
            models: /groups\/data\/model\/(\?|$)/
        },
        models = {
            fields: {
                name: {
                    required: true,
                    sortable: true,
                    maxLength: 10
                },
                description: {
                    sortable: false,
                    maxLength: 20
                }
            }
        }; 
        
    
    fixtures.groups = {
        data: data,
        urls: urls,
        models: models
    };

    fixtures.responses.push(['GET', urls.models,
        [200, fixtures.headers, JSON.stringify(models)]
    ]);
    fixtures.responses.push(['GET', urls.fetchOne,
        function (xhr, id) {
            xhr.respond(200, fixtures.headers, JSON.stringify(
                _.where(data, {id: id})
            ));
        }
    ]);
    fixtures.responses.push(['GET', urls.fetchAll,
        [200, fixtures.headers, JSON.stringify(data)]
    ]);
    fixtures.responses.push(['POST', urls.create,
        function (xhr) {
            xhr.respond(200, fixtures.headers, xhr.requestBody);
        }
    ]);
    fixtures.responses.push(['PUT', urls.save,
        function (xhr) {
            xhr.respond(200, fixtures.headers, xhr.requestBody);
        }
    ]);

    return fixtures;
});