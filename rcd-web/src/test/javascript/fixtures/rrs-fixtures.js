define(function(require){

    var _ = require('underscore'),
        fixtures = require('base-fixtures');

    var data = [
            {
                "defaultRecordId": null,
                "fromDictionary": {"name": "my2", "id": "dictionary1", "description": "ee334412233", "version": null, "writable": true, "code": null, "groupId": "group1id"},
                "toDictionary": {"name": "1", "id": "esnsi_11", "description": null, "version": "7", "writable": false, "code": "1", "groupId": "esnsi_1"},
                "fromGroup": {
                    "name": "group1",
                    "id": "group1id",
                    "owner": null,
                    "description": "group1 description"
                },
                "toGroup": {
                    "name": "Default group",
                    "id": "esnsi_1",
                    "owner": "ESNSI",
                    "description": null
                },
                "name": "rrs1name",
                "id": "rrs1",
                "fromMetaFieldId": "metafield1",
                "toMetaFieldId": "metafield3"
            },
            {
                "defaultRecordId": null,
                "fromDictionary": {"name": "1", "id": "esnsi_11", "description": null, "version": "7", "writable": false, "code": "1", "groupId": "esnsi_1"},
                "toDictionary": {"name": "my2", "id": "dictionary1", "description": "ee334412233", "version": null, "writable": true, "code": null, "groupId": "group1id"},
                "fromGroup": {
                    "name": "Default group",
                    "id": "esnsi_1",
                    "owner": "ESNSI",
                    "description": null
                },
                "toGroup": {
                    "name": "group1",
                    "id": "group1id",
                    "owner": null,
                    "description": "group1 description"
                },
                "name": "rrs2name",
                "id": "rrs2",
                "fromMetaFieldId": "metafield4",
                "toMetaFieldId": "metafield2"
            }
        ],
        urls = {
            fetchAll: /rrs\/data\/\?dictionaryId=([\w-]+)(&direction=(\w+))?(&|$)/,
            fetchOne: /rrs\/data\/([\w-]+)(\/$|\?|$)/,
            save: /rrs\/data\/([\w-]+)(\/$|\?|$)/,
            create: /rrs\/data\/(\?|$)/,
            delete: /rrs\/data\/([\w-]+)(\/$|\?|$)/
        },
        dataOfDictionary = function(dictionaryId, direction){
            return _.filter(data, function(rrs){
                switch (direction) {
                    case 'FROM':
                        return rrs.fromDictionary.id == dictionaryId;
                    case 'TO':
                        return rrs.toDictionary.id == dictionaryId;
                    default:
                        return rrs.fromDictionary.id == dictionaryId || rrs.toDictionary.id == dictionaryId;
                }
            })
        };

    fixtures.rrs = {
        data: data,
        urls: urls,
        dataOfDictionary: dataOfDictionary
    };

    fixtures.responses.push(['GET', urls.fetchOne,
        function(xhr, id){
            xhr.respond(200, fixtures.headers, JSON.stringify(
                _.findWhere(data, {id: id})
            ));
        }
    ]);
    fixtures.responses.push(['GET', urls.fetchAll,
        function(xhr, dictionaryId, _dir, direction){
            xhr.respond(200, fixtures.headers, JSON.stringify(
                dataOfDictionary(dictionaryId, direction)
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