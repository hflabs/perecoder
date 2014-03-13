define(function(require) {

    var fixtures = require('base-fixtures');

    var data = [
            {
                "rrs": {
                    "name": "test",
                    "id": "fee5c110-de41-49aa-beae-0fca5898d5cd"
                },
                "fromGroup": {
                    "name": "OW",
                    "id": "d049c0ab-2db7-4253-b323-b443bc1e9858"
                },
                "fromDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "80ea9d94-d594-475f-a645-c0a8c3760c73"
                },
                "toGroup": {
                    "name": "CDI",
                    "id": "e26da9bd-4d56-4d09-8c32-ecc7f231d6d9"
                },
                "toDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "8d1e6a26-3f30-429d-8925-1b4ab08a9fc6"
                },
                "historyId": "5db7dcca-3ff5-419e-af64-5dbd4e161235",
                "fromValue": "test_value1",
                "startDate": 1389884164566,
                "endDate": 1389884224566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "5bfb4164-3cfd-4921-870e-9bccb217f2f7",
                "type": "NO_VALUE",
                "count": 1
            },
            {
                "rrs": {
                    "name": "test1",
                    "id": null
                },
                "fromGroup": {
                    "name": null,
                    "id": null
                },
                "fromDictionary": {
                    "name": null,
                    "id": null
                },
                "toGroup": {
                    "name": null,
                    "id": null
                },
                "toDictionary": {
                    "name": null,
                    "id": null
                },
                "historyId": "3ff62da8-7e3a-47bd-b48b-4a8bd0ef565f",
                "fromValue": null,
                "startDate": 1389884164566,
                "endDate": 1389884224566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "820ef5b4-969b-439d-b74b-f2681a65ead5",
                "type": "NO_RULE_ALIAS",
                "count": 1
            },
            {
                "rrs": {
                    "name": "test",
                    "id": "fee5c110-de41-49aa-beae-0fca5898d5cd"
                },
                "fromGroup": {
                    "name": "OW",
                    "id": "d049c0ab-2db7-4253-b323-b443bc1e9858"
                },
                "fromDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "80ea9d94-d594-475f-a645-c0a8c3760c73"
                },
                "toGroup": {
                    "name": "CDI",
                    "id": "e26da9bd-4d56-4d09-8c32-ecc7f231d6d9"
                },
                "toDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "8d1e6a26-3f30-429d-8925-1b4ab08a9fc6"
                },
                "historyId": "ce623a0e-5a3b-4c71-b4e2-7203952ece5b",
                "fromValue": "test_value",
                "startDate": 1389884104566,
                "endDate": 1389884164566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "7df79a79-71f4-4fd5-8663-26653a0db355",
                "type": "NO_RULE",
                "count": 1
            },
            {
                "rrs": {
                    "name": null,
                    "id": null
                },
                "fromGroup": {
                    "name": "OW",
                    "id": "d049c0ab-2db7-4253-b323-b443bc1e9858"
                },
                "fromDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "80ea9d94-d594-475f-a645-c0a8c3760c73"
                },
                "toGroup": {
                    "name": "CR",
                    "id": "078fe440-8a8d-4482-bca5-6a13d17e7bed"
                },
                "toDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "8beaa1dc-7673-4c95-aa77-379b4db5d62e"
                },
                "historyId": "a4e75887-e62d-469f-a3e1-4fc00fcfd060",
                "fromValue": "test_value",
                "startDate": 1389884104566,
                "endDate": 1389884164566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "a9338d51-720f-4075-a064-153c253d7485",
                "type": "NO_RULE_SET",
                "count": 1
            },
            {
                "rrs": {
                    "name": null,
                    "id": null
                },
                "fromGroup": {
                    "name": "OW",
                    "id": "d049c0ab-2db7-4253-b323-b443bc1e9858"
                },
                "fromDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "80ea9d94-d594-475f-a645-c0a8c3760c73"
                },
                "toGroup": {
                    "name": "CDI",
                    "id": "e26da9bd-4d56-4d09-8c32-ecc7f231d6d9"
                },
                "toDictionary": {
                    "name": "ADDRESS_TYPE1",
                    "id": null
                },
                "historyId": "fc776738-5a0b-4956-b855-c3280d3182f5",
                "fromValue": "test_value",
                "startDate": 1389884044566,
                "endDate": 1389884104566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "48ede916-1a3e-4303-9566-d98dce1fb2ad",
                "type": "NO_DICTIONARY",
                "count": 1
            },
            {
                "rrs": {
                    "name": "test",
                    "id": "fee5c110-de41-49aa-beae-0fca5898d5cd"
                },
                "fromGroup": {
                    "name": "OW",
                    "id": "d049c0ab-2db7-4253-b323-b443bc1e9858"
                },
                "fromDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "80ea9d94-d594-475f-a645-c0a8c3760c73"
                },
                "toGroup": {
                    "name": "CDI",
                    "id": "e26da9bd-4d56-4d09-8c32-ecc7f231d6d9"
                },
                "toDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "8d1e6a26-3f30-429d-8925-1b4ab08a9fc6"
                },
                "historyId": "9e5e0cd0-1b4a-49ef-9d52-c2188a3423cc",
                "fromValue": "test_value1",
                "startDate": 1389884044566,
                "endDate": 1389884104566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "98a3e71b-b140-46ff-95e2-49402d2da78c",
                "type": "NO_VALUE",
                "count": 1
            },
            {
                "rrs": {
                    "name": null,
                    "id": null
                },
                "fromGroup": {
                    "name": "OW",
                    "id": "d049c0ab-2db7-4253-b323-b443bc1e9858"
                },
                "fromDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "80ea9d94-d594-475f-a645-c0a8c3760c73"
                },
                "toGroup": {
                    "name": "CDI1",
                    "id": null
                },
                "toDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": null
                },
                "historyId": "5caa9667-b175-4ddc-a4c5-ac2f2e2253c3",
                "fromValue": "test_value",
                "startDate": 1389884044566,
                "endDate": 1389884104566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "a41d9a6d-3f59-4d4e-b8fd-642ebaea3681",
                "type": "NO_GROUP",
                "count": 1
            },
            {
                "rrs": {
                    "name": null,
                    "id": null
                },
                "fromGroup": {
                    "name": "OW",
                    "id": "d049c0ab-2db7-4253-b323-b443bc1e9858"
                },
                "fromDictionary": {
                    "name": "ADDRESS_TYPE1",
                    "id": null
                },
                "toGroup": {
                    "name": "CDI",
                    "id": "e26da9bd-4d56-4d09-8c32-ecc7f231d6d9"
                },
                "toDictionary": {
                    "name": "ADDRESS_TYPE1",
                    "id": null
                },
                "historyId": "3eb88845-e349-4421-9103-a7325d68fad0",
                "fromValue": "test_value",
                "startDate": 1389884044566,
                "endDate": 1389884104566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "f52c1196-7702-4222-9662-587aea675c8b",
                "type": "NO_DICTIONARY",
                "count": 1
            },
            {
                "rrs": {
                    "name": null,
                    "id": null
                },
                "fromGroup": {
                    "name": "OW1",
                    "id": null
                },
                "fromDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": null
                },
                "toGroup": {
                    "name": "CDI",
                    "id": "e26da9bd-4d56-4d09-8c32-ecc7f231d6d9"
                },
                "toDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "8d1e6a26-3f30-429d-8925-1b4ab08a9fc6"
                },
                "historyId": "5aea2112-7b53-4a70-b515-8c541b34f3f1",
                "fromValue": "test_value",
                "startDate": 1389883984566,
                "endDate": 1389884044566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "039de96c-6cc2-4ee2-9623-b1d1b448ad27",
                "type": "NO_GROUP",
                "count": 1
            },
            {
                "rrs": {
                    "name": "test",
                    "id": "fee5c110-de41-49aa-beae-0fca5898d5cd"
                },
                "fromGroup": {
                    "name": "OW",
                    "id": "d049c0ab-2db7-4253-b323-b443bc1e9858"
                },
                "fromDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "80ea9d94-d594-475f-a645-c0a8c3760c73"
                },
                "toGroup": {
                    "name": "CDI",
                    "id": "e26da9bd-4d56-4d09-8c32-ecc7f231d6d9"
                },
                "toDictionary": {
                    "name": "ADDRESS_TYPE",
                    "id": "8d1e6a26-3f30-429d-8925-1b4ab08a9fc6"
                },
                "historyId": "1ce8d192-5dc4-41ae-a19c-433a204a2a42",
                "fromValue": "test_value",
                "startDate": 1389883984566,
                "endDate": 1389884044566,
                "processingDate": null,
                "processingState": "PENDING",
                "processingAuthor": null,
                "id": "52828ba8-13d1-46e5-af7f-9d366285e750",
                "type": "ERROR",
                "count": 1
            }
        ],
        urls = {
            fetchAll: /notifications\/data\/(\?|$)/,
            save: /notifications\/data\/(\?|$)/
        };

    fixtures.notifications = {
        data: data,
        urls: urls
    };

    fixtures.responses.push(['GET', urls.fetchAll,
        function(xhr){
            xhr.respond(200, fixtures.headers, JSON.stringify(data));
        }
    ]);
    fixtures.responses.push(['PUT', urls.save,
        function(xhr){
            xhr.respond(200, fixtures.headers, JSON.stringify(xhr.requestBody));
        }
    ]);

    return fixtures;
});
