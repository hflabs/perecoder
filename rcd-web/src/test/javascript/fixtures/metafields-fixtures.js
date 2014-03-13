define(function(require){

    var _ = require('underscore'),
        fixtures = require('base-fixtures');

    var data = [
            {
                "name": "CODE",
                "id": "metafield1",
                "type": "STRING",
                "hidden": false,
                "description": null,
                "writable": true,
                "unique": true,
                "dictionaryId": "dictionary1",
                "primary": true
            },
            {
                "name": "Field 2",
                "id": "metafield2",
                "type": "STRING",
                "hidden": false,
                "description": "field 2 description",
                "writable": true,
                "unique": false,
                "dictionaryId": "dictionary1",
                "primary": false
            },
            {
                "name": "CODE",
                "id": "metafield3",
                "type": "STRING",
                "hidden": false,
                "description": null,
                "writable": true,
                "unique": true,
                "dictionaryId": "esnsi_11",
                "primary": true
            },
            {
                "name": "Field 4",
                "id": "metafield4",
                "type": "STRING",
                "hidden": false,
                "description": "field 4 description",
                "writable": true,
                "unique": false,
                "dictionaryId": "esnsi_11",
                "primary": false
            }
        ],
        urls = {
            fetchAll: /metafields\/data\/\?dictionaryId=([\w-]+)(&|$)/,
            save: /metafields\/data\/([\w-]+)(\/$|\?|$)/,
            create: /metafields\/data\/(\?|$)/,
            delete: /metafields\/data\/([\w-]+)(\/$|\?|$)/
        },
        dataOfDictionary = function(dictionaryId){
            return _.where(data, {dictionaryId: dictionaryId});
        };

    fixtures.metafields = {
        data: data,
        urls: urls,
        dataOfDictionary: dataOfDictionary
    };

    fixtures.responses.push(['GET', urls.fetchAll,
        function(xhr, dictionaryId){
            xhr.respond(200, fixtures.headers, JSON.stringify(
                dataOfDictionary(dictionaryId)
            ));
        }
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
    fixtures.responses.push(['DELETE', urls.delete, ""]);

    return fixtures;
});