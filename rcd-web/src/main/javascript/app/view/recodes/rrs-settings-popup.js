'use strict';

define(function (require) {
    var _ = require('underscore'),
        Marionette = require('marionette'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        EmptyModel = require('model/common/empty-model'),
        RecordsList = require('model/content/records-list'),
        RecordSelectorView = require('view/recodes/record-selector-view'),
        ViewHelpers = require('view/view-helpers'),
        viewTemplate = require('text!template/recodes/rrs-settings-popup.html');

    var defaultHints = {

    };

    /**
     * Recode-rule-sets' settings layout.
     * Handles all the UI-only logic.
     * @class
     */
    var RRSSettingsPopup = Marionette.Layout.extend({
        className: 'modal fade in',
        attributes: {style: 'display:none'},
        template: _.template(viewTemplate),
        regions: {
            selectorRegion: '[data-region="record-selector"]'
        },
        ui: {
            name: '[data-field="name"]',
            fromMetafield: '[data-field="from-metafield"]',
            toMetafield: '[data-field="to-metafield"]',
            alert: '.alert',
            selects: '.selectpicker',
            inputs: ':text',
            controls: '[data-field]',
            btnSave: '[data-action="save"]'
        },
        events: {
            'change [data-field]': 'onFieldChanged',
            'keyup input:text[data-field]': 'onFieldChanged',
            'cut input:text[data-field]': 'onFieldChanged',
            'paste input:text[data-field]': 'onFieldChanged',
            'click [data-action="save"]': 'onSubmit'
        },
        modelEvents: {
            'invalid': 'onInvalid'
        },
        templateHelpers: function(){
            var self = this;
            return _.extend({
                getFromMetafields: function(){
                    return self.fromMetafields;
                },
                getToMetafields: function(){
                    return self.toMetafields;
                },
                getDefaultHint: function(field){
                    return defaultHints[field] || '';
                }
            }, ViewHelpers.templateHelpers);
        },
        initialize: function (options) {
            this.fromMetafields = options.fromMetafields;
            this.toMetafields = options.toMetafields;

            this.emptyModel = new EmptyModel({
                text: i18n.tc('view.recodes.settings.default.off')
            });
            this.dropdownRecords = this.createDropdownRecords();
            this.records = this.createDefaultList();

            this.recordSelectorView = new RecordSelectorView({
                metafields: this.toMetafields,
                records: this.records,
                dropdownRecords: this.dropdownRecords,
                resetText: i18n.tc('view.recodes.settings.default.unset'),
                $container: this.$el
            });

            this.invalidFields = {};
        },
        onShow: function(){
            this.$el.modal('show');
            this.resetInputsSelection();
            this.ui.selects.selectpicker();//.change();
            this.selectorRegion.show(this.recordSelectorView);
        },
        onClose: function(){
            this.$el.modal('hide');
        },

        /* private */

        resetInputsSelection: function(){
            this.ui.inputs.each(function(){
                this.value = this.value;
            });
        },

        createDropdownRecords: function(){
            var list = new RecordsList(null, { dictionaryId: this.model.get('toDictionary').id });
            this.listenTo(list, {
                'recordselector:selected': this.onRecordSelectorSelected
            });
            return list;
        },

        createDefaultList: function(){
            var list = new RecordsList(null, { dictionaryId: this.model.get('toDictionary').id });
            this.listenTo(list, {
                'recordselector:select': this.onRecordSelectorSelect,
                'recordselector:unselect': this.onRecordSelectorUnselect
            });
            this.defaultRecordId = this.model.get('defaultRecordId');
            if (this.defaultRecordId) {
                list.fetch({ data: { recordIDs: this.defaultRecordId } });
            } else {
                var emptyModel = this.emptyModel;
                _.defer(function(){
                    list.reset(emptyModel);
                });
            }
            return list;
        },

        /**
         * When user clicks table row
         * @param model
         */
        onRecordSelectorSelect: function(model){
            var at = 0, count = 1;
            this.recordSelectorView.showDropdown(at, count);
        },

        onRecordSelectorUnselect: function(){
            this.recordSelectorView.hideDropdown();
        },

        /**
         * @param id ID of selected record or null
         */
        onRecordSelectorSelected: function(id){
            this.defaultRecordId = id;
            var model = this.dropdownRecords.get(id);
            this.records.reset(model || this.emptyModel);
            this.recordSelectorView.hideDropdown();
        },

        /**
         * Shows errors if model changing failed
         */
        onInvalid: function(){
            this.putErrors(this.model.validationError);
        },

        /**
         * Validates field's value when it is changed
         * @param {Event} e
         */
        onFieldChanged: function(e){
            var $control = $(e.target),
                field = $control.attr('data-field');
            this.putError(field, $control);
        },

        onSubmit: function(){
            var name = this.ui.name.val().trim();
            var fromMetafieldId = this.ui.fromMetafield.val();
            var toMetafieldId = this.ui.toMetafield.val();
            var handler = _.bind(this.putErrors, this);
            var xhr = this.model.save({
                    'name': name,
                    'fromDictionaryId': this.model.get('fromDictionary').id,
                    'toDictionaryId': this.model.get('toDictionary').id,
                    'fromMetaFieldId': fromMetafieldId,
                    'toMetaFieldId': toMetafieldId,
                    'defaultRecordId': this.defaultRecordId
                }, {
                    wait: true,
                    patch: true,
                    type: 'PUT',
                    globalErrorsHandler: handler,
                    fieldErrorsHandler: handler
                }),
                self = this;
            if (xhr) {
                xhr.done(function(){
                    self.onClose();
                });
            }
        },

        hasInvalidFields: function(){
            return _.compact(_.values(this.invalidFields)).length > 0;
        },

        /**
         * Enables/disables button depending on errors presence
         */
        updateSaveBtn: function(){
            this.ui.btnSave.attr('disabled', this.hasInvalidFields());
        },

        /**
         * Puts or clears error message
         * @param {String} field
         * @param {$} $control
         * @param {ErrorModel} errors
         */
        putError: function(field, $control, errors){
            errors = errors || {};
            errors.fieldErrors = errors.fieldErrors || {};
            var msg = errors.fieldErrors[field];
            $control.closest('.control-group').toggleClass('error', !!msg)
                .find('.help-block').text(msg || '');
            this.invalidFields[field] = !!msg;
            this.updateSaveBtn();
        },

        /**
         * Puts errors to UI
         * @param {ErrorModel} errors
         */
        putErrors: function(errors){
            errors = errors || {};
            errors.globalErrors = errors.globalErrors || [];
            this.ui.alert.toggle(errors.globalErrors.length > 0)
                .toggleClass('error', errors.globalErrors.length > 0)
                .text( errors.globalErrors.join(', ') );
            var self = this;
            this.ui.controls.each(function(){
                var $control = $(this),
                    field = $control.attr('data-field');
                self.putError(field, $control, errors);
            });
        },

    });

    return RRSSettingsPopup;

});
