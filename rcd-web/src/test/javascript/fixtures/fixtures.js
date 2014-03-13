define(function(require){

    var fixtures = require('base-fixtures');

    require('groups-fixtures');
    require('dictionaries-fixtures');
    require('metafields-fixtures');
    require('records-fixtures');
    require('tasks-fixtures');
    require('notifications-fixtures');
    require('rrs-fixtures');
    require('recodes-fixtures');

    return fixtures;

});