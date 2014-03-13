define(function(require){

    var _ = require('underscore'),
        fixtures = require('base-fixtures');

    var data = [
            {
                "descriptor": {
                    "id": "06d9fc7b-9fd3-4b84-bf68-b901ac44340d",
                    "historyId": "f7e11eeb-cf2c-4aea-bd63-d0c108959e72",
                    "name": "cnsiSyncTaskPerformer",
                    "description": "CNSI task",
                    "cron": "* 3 * * * *",
                    "nextScheduledDate": null,
                    "parameters": {
                        "groupName": "MyCRM",
                        "url": "http://localhost:8080/MyCRM",
                        "timeout": "1000"
                    }
                },
                "result": {
                    "id": "06d9fc7b-9fd3-4b84-bf68-b901ac44340d",
                    "owner": "af620897a8f36430f24468beb4fb6060",
                    "author": "HFLabs-RCD",
                    "registrationDate": 1379685839001,
                    "startDate": 1379685839002,
                    "endDate": 1379685839011,
                    "status": "ERROR",
                    "errorMessage": "localhost34",
                    "parameters": {
                        "groupName": "MyCRM",
                        "url": "http://localhost:8080/MyCRM",
                        "timeout": "1000"
                    },
                    "content": null
                },
                "status": "READY",
                "progress": null,
                "id": "06d9fc7b-9fd3-4b84-bf68-b901ac44340d"
            },
            {
                "descriptor": {
                    "id": "563b6f21-a575-4d40-b274-8541363d2c67",
                    "historyId": "8798af6b-b094-45a7-8e31-0eaa2b9db5a6",
                    "name": "dummyTaskPerformer",
                    "description": "dummy task",
                    "cron": "0 * * * * *",
                    "nextScheduledDate": null, //1379676300000,
                    "parameters": {
                        "count": "2100",
                        "delay": "10"
                    }
                },
                "result": null,
                "status": "READY",
                "progress": {
                    "percent": 45,
                    "step": "Итерация 942"
                },
                "id": "563b6f21-a575-4d40-b274-8541363d2c67"
            },
            {
                "descriptor": {
                    "id": "9ebd973f-20d5-40d8-b1f5-39d3fcbd36c2",
                    "historyId": "51c586df-4bbe-4454-954d-61544e2891e5",
                    "name": "indexRebuildTaskPerformer",
                    "description": "Index task",
                    "cron": null,
                    "nextScheduledDate": null,
                    "parameters": {
                        "target": "",
                        "force": true
                    }
                },
                "result": {
                    "id": "9ebd973f-20d5-40d8-b1f5-39d3fcbd36c2",
                    "owner": "af620897a8f36430f24468beb4fb6060",
                    "author": "admin",
                    "registrationDate": 1379416058867,
                    "startDate": 1379416058869,
                    "endDate": 1379416058911,
                    "status": "FINISHED",
                    "errorMessage": null,
                    "parameters": {

                    },
                    "content": {
                        "indexes": [{
                            "status": "SKIPPED",
                            "targetClass": "ru.hflabs.rcd.model.change.History",
                            "documentCount": 134
                        },
                            {
                                "status": "SKIPPED",
                                "targetClass": "ru.hflabs.rcd.model.notification.Notification",
                                "documentCount": 0
                            },
                            {
                                "status": "SKIPPED",
                                "targetClass": "ru.hflabs.rcd.model.document.Group",
                                "documentCount": 2
                            },
                            {
                                "status": "SKIPPED",
                                "targetClass": "ru.hflabs.rcd.model.document.Dictionary",
                                "documentCount": 8
                            },
                            {
                                "status": "SKIPPED",
                                "targetClass": "ru.hflabs.rcd.model.document.MetaField",
                                "documentCount": 14
                            },
                            {
                                "status": "SKIPPED",
                                "targetClass": "ru.hflabs.rcd.model.document.Field",
                                "documentCount": 51
                            },
                            {
                                "status": "SKIPPED",
                                "targetClass": "ru.hflabs.rcd.model.rule.RecodeRuleSet",
                                "documentCount": 0
                            },
                            {
                                "status": "SKIPPED",
                                "targetClass": "ru.hflabs.rcd.model.rule.RecodeRule",
                                "documentCount": 0
                            },
                            {
                                "status": "SKIPPED",
                                "targetClass": "ru.hflabs.rcd.model.task.TaskDescriptor",
                                "documentCount": 3
                            },
                            {
                                "status": "SKIPPED",
                                "targetClass": "ru.hflabs.rcd.model.task.TaskResult",
                                "documentCount": 20
                            }]
                    }
                },
                "status": "READY",
                "progress": null,
                "id": "9ebd973f-20d5-40d8-b1f5-39d3fcbd36c2"
            }
        ],
        urls = {
            fetchAll: /tasks\/data\/(\?|$)/,
            fetchOne: /tasks\/data\/([\w-]+)(\?|$)/,
            fetchExecuting: /tasks\/data\/executing\/(\?|$)/,
            save: /tasks\/data\/([\w-]+)(\?|$)/,
            execute: /tasks\/data\/([\w-]+)\/execute(\?|$)/,
            cancel: /tasks\/data\/([\w-]+)\/cancel(\?|$)/,
            models: /tasks\/data\/model\/(\?|$)/
        },

        models = [
            {
                "id": "dummyTaskPerformer",
                "fields": {
                    "parameters.count": {
                        "type": "NUMBER",
                        "minLength": 1,
                        "maxLength": 2147483647,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.delay": {
                        "type": "NUMBER",
                        "minLength": 1,
                        "maxLength": 9223372036854775807,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.errorMessage": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "name": {
                        "type": "STRING",
                        "minLength": 1,
                        "maxLength": 255,
                        "required": true,
                        "pattern": null,
                        "sortable": true
                    },
                    "description": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 1000,
                        "required": true,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "nextScheduledDate": {
                        "type": "DATE",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "cron": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    },
                    "historyId": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 36,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    },
                    "id": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 36,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    }
                },
                "permissions": 3,
                "defaultParameters": {
                    "count": 1,
                    "delay": 1000,
                    "errorMessage": null
                },
                "availableValues": null,
                "deletable": true
            },
            {
                "id": "cnsiSyncTaskPerformer",
                "fields": {
                    "parameters.url": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": true,
                        "pattern": "^((?:https?)://)?(?:\\S+(?::\\S*)?@)?(?:(?!10(?:\\.\\d{1,3}){3})(?!127(?:\\.\\d{1,3}){3})(?!169\\.254(?:\\.\\d{1,3}){2})(?!192\\.168(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*)(?::\\d{2,5})?(?:/[^\\s]*)?",
                        "sortable": false
                    },
                    "parameters.timeout": {
                        "type": "NUMBER",
                        "minLength": 0,
                        "maxLength": 9223372036854775807,
                        "required": true,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.poolSize": {
                        "type": "NUMBER",
                        "minLength": -1,
                        "maxLength": 2147483647,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "name": {
                        "type": "STRING",
                        "minLength": 1,
                        "maxLength": 255,
                        "required": true,
                        "pattern": null,
                        "sortable": true
                    },
                    "description": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 1000,
                        "required": true,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "nextScheduledDate": {
                        "type": "DATE",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "cron": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    },
                    "historyId": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 36,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    },
                    "id": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 36,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    }
                },
                "permissions": 1,
                "defaultParameters": {
                    "url": "http://localhost:8080/cnsi",
                    "timeout": 300,
                    "poolSize": -1
                },
                "availableValues": null,
                "deletable": false
            },
            {
                "id": "indexRebuildTaskPerformer",
                "fields": {
                    "parameters.target": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.force": {
                        "type": "BOOLEAN",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "name": {
                        "type": "STRING",
                        "minLength": 1,
                        "maxLength": 255,
                        "required": true,
                        "pattern": null,
                        "sortable": true
                    },
                    "description": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 1000,
                        "required": true,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "nextScheduledDate": {
                        "type": "DATE",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "cron": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    },
                    "historyId": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 36,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    },
                    "id": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 36,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    }
                },
                "permissions": 3,
                "defaultParameters": {
                    "target": null,
                    "force": false
                },
                "availableValues": null,
                "deletable": true
            },
            {
                "id": "dataSourceSyncTaskPerformer",
                "fields": {
                    "parameters.driverName": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": true,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.password": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.username": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.jdbcUrl": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": true,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.jdbcProperties": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.tocTableName": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": true,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.timeout": {
                        "type": "NUMBER",
                        "minLength": 0,
                        "maxLength": 9223372036854775807,
                        "required": true,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters.poolSize": {
                        "type": "NUMBER",
                        "minLength": -1,
                        "maxLength": 2147483647,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "name": {
                        "type": "STRING",
                        "minLength": 1,
                        "maxLength": 255,
                        "required": true,
                        "pattern": null,
                        "sortable": true
                    },
                    "description": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 1000,
                        "required": true,
                        "pattern": null,
                        "sortable": false
                    },
                    "parameters": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "nextScheduledDate": {
                        "type": "DATE",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": false
                    },
                    "cron": {
                        "type": "STRING",
                        "minLength": null,
                        "maxLength": null,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    },
                    "historyId": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 36,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    },
                    "id": {
                        "type": "STRING",
                        "minLength": 0,
                        "maxLength": 36,
                        "required": false,
                        "pattern": null,
                        "sortable": true
                    }
                },
                "permissions": 3,
                "defaultParameters": {
                    "driverName": null,
                    "jdbcUrl": null,
                    "jdbcProperties": null,
                    "username": null,
                    "password": null,
                    "tocTableName": null,
                    "timeout": 300,
                    "poolSize": -1
                },
                "availableValues": {
                    "driverName": {
                        "sybase": {
                            "name": "sybase",
                            "description": "Sybase",
                            "urlTemplate": "jdbc:jtds:sybase://host[:port][/database]"
                        },
                        "vertica": {
                            "name": "vertica",
                            "description": "Vertica database",
                            "urlTemplate": "jdbc:vertica://host:port/database"
                        },
                        "mssql": {
                            "name": "mssql",
                            "description": "Microsoft SQL server",
                            "urlTemplate": "jdbc:jtds:sqlserver://host[:port][/database]"
                        },
                        "mysql": {
                            "name": "mysql",
                            "description": "MySQL",
                            "urlTemplate": "jdbc:mysql://host[,failoverhost][:port]/[database]"
                        },
                        "oracle": {
                            "name": "oracle",
                            "description": "Oracle",
                            "urlTemplate": "jdbc:oracle:thin:host:port:SID"
                        },
                        "pgsql": {
                            "name": "pgsql",
                            "description": "PostgreSQL",
                            "urlTemplate": "jdbc:postgresql://host:port/database"
                        },
                        "mariadb": {
                            "name": "mariadb",
                            "description": "MariaDB",
                            "urlTemplate": "jdbc:mariadb://host[,failoverhost][:port]/[database]"
                        }
                    }
                },
                "deletable": true
            }
        ];

    fixtures.tasks = {
        data: data,
        urls: urls,
        models: models,

        descriptorCNSI: {
            id: '06d9fc7b-9fd3-4b84-bf68-b901ac44340d',
            name: 'cnsiSyncTaskPerformer',
            cron: '* * * 1 2 3',
            description: 'CNSI descriptor',
            nextScheduledDate: 1379492159002,
            parameters: {
                groupName: 'MyCRM',
                url: 'http://localhost:8080/path',
                timeout: 0
            }
        },
        descriptorDummy: {
            id: '06d9fc7b-9fd3-4b84-bf68-b901ac44340d',
            name: 'dummyTaskPerformer',
            description: 'Dummy descriptor',
            parameters: {
                count: 1,
                delay: 1
            }
        }
    };

    fixtures.responses.push(['GET', urls.models,
        function(xhr){
            xhr.respond(200, fixtures.headers, JSON.stringify(models));
        }
    ]);
    fixtures.responses.push(['GET', urls.fetchAll,
        function(xhr){
            xhr.respond(200, fixtures.headers, JSON.stringify(data));
        }
    ]);
    fixtures.responses.push(['GET', urls.fetchExecuting,
        function(xhr){
            xhr.respond(
                200,
                fixtures.headers,
                JSON.stringify(
                    _.filter(data, function(task){
                        return task.status == 'RUNNING' || task.status == 'INTERRUPTING';
                    })
                )
            );
        }
    ]);
    fixtures.responses.push(['GET', urls.fetchOne,
        function(xhr, id){
            xhr.respond(200, fixtures.headers, JSON.stringify(
                _.findWhere(data, {id: id})
            ));
        }
    ]);
    fixtures.responses.push(['PUT', urls.save,
        function(xhr, id){
            xhr.respond(410, fixtures.headers, JSON.stringify(
                {
                    globalErrors: ['Object moved']
                }
            ));
        }
    ]);
    fixtures.responses.push(['PUT', urls.execute,
        function(xhr, id){
            xhr.respond(
                200,
                fixtures.headers,
                JSON.stringify(
                    _.extend(
                        _.findWhere(data, {id: id}),
                        {status: 'RUNNING'}
                    )
                )
            );
        }
    ]);
    fixtures.responses.push(['PUT', urls.cancel,
        function(xhr, id){
            xhr.respond(
                200,
                fixtures.headers,
                JSON.stringify(
                    _.extend(
                        _.findWhere(data, {id: id}),
                        {status: 'INTERRUPTING'}
                    )
                )
            );
        }
    ]);

    return fixtures;
});
