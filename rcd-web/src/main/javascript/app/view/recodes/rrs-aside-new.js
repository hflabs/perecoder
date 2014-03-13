'use strict';

define(function(require){
    var _ = require('underscore'),
        ListView = require('view/list-view'),
        i18n = require('locale/i18n'),
        RRSAsideNewGroupView = require('view/recodes/rrs-aside-new-group'),
        viewTemplate = require('text!template/recodes/rrs-aside-new.html');

    /**
     * View of groups and dictionaries tree
     * @class
     */
    var RRSAsideNewView = ListView.extend({
        template: _.template(viewTemplate),
        itemView: RRSAsideNewGroupView,
        itemViewContainer: '.groups',
        ui: {
            filter: '[data-field="filter"]'
        },
        events: {
            'keypress [data-field="filter"]': 'onFilterKeyup',
            'click .searchbar .search': 'onApplyFilter',
            'click .searchbar .clear': 'onClearFilter'
        },
        triggers: {
            'click [data-action="cancel"]': 'aside:previous'
        },

        /* ===== Filter methods ===== */

        applyFilter: function(pattern){
            this.collection.each(function(model){
                model.trigger('filter', pattern);
            });
        },
        onFilterKeyup: function(e){
            if (e && (e.keyCode || e.which) == 13) {
                this.onApplyFilter();
            }
        },
        onApplyFilter: function(){
            this.applyFilter(this.ui.filter.val().trim());
        },
        onClearFilter: function(){
            this.ui.filter.val('');
            this.applyFilter();
        }
    });

    return RRSAsideNewView;
});