'use strict';

define(function(require) {
    var $ = require('jquery'),
        _ = require('underscore'),
        routes = require('routes'),
        i18n = require('locale/i18n'),
        ItemView = require('view/item-view'),
        ViewHeplers = require('view/view-helpers'),
        editTemplate = require('text!template/dictionaries/dictionary-popup-upload-view.html');

    var DictionaryPopupUploadView = ItemView.extend({
        className: 'modal fade in',
        attributes: {style: 'display:none'},
        templateHelpers: function(){
            var self = this;
            return _.extend({
                getGroupsList: function(){
                    return self.groupsList;
                },
                getEncodings: function(){
                    return self.model.modelParams.availableValues.encoding || [];
                }
            }, ViewHeplers.templateHelpers);
        },
        template: _.template(editTemplate),
        events: {
            'change [data-field]': 'onFieldChanged',
            'keyup input:text[data-field]': 'onFieldChanged',
            'cut input:text[data-field]': 'onFieldChanged',
            'paste input:text[data-field]': 'onFieldChanged',
            'click [data-action="submit"]': 'onSubmit'
        },
        modelEvents: {
            'invalid': 'onInvalid'
        },
        ui: {
            alert: '.alert',
            form: 'form',
            controls: '[data-field]',
            name: '[data-field="name"]',
            selects: 'select.selectpicker',
            btnSave: '[data-action="submit"]'
        },
        initialize: function(options){
            this.groupsList = options.groupsList;
        },
        onShow: function(){
            this.$el.modal('show');
        },
        onClose: function(){
            this.$el.modal('hide');
        },
        onRender: function(){
            this.ui.selects.selectpicker();
            this.applyModelParams();
            this.setInvalidFields();
            this.updateSaveBtn();
        },

        setInvalidFields: function(){
            var fields = this.getFieldsValues(),
                errors = this.model.validate(fields),
                result = {};
            _.each(errors && errors.fieldErrors, function(msg, field){
                result[field] = true;
            });
            this.invalidFields = result;
        },

        getFieldsValues: function(){
            var fields = {};
            this.ui.controls.each(function(){
                var $control = $(this),
                    field = $control.attr('data-field');
                fields[field] = $control.val().trim();
            });
            return fields;
        },

        onSubmit: function(){
            var attrs = this.getFieldsValues();
            if (this.model.set(attrs, {validate: true})) {
                this.ui.btnSave.attr('disabled', true);
                var self = this,
                    xhr = this.ui.form.ajaxSubmit({
                        url: routes.buildUrl('DICTIONARIES_UPLOAD', this.model.id)
                    }).data('jqxhr');
                if (xhr) {
                    xhr.done(function(resp, status, xhr){
                        self.ui.btnSave.attr('disabled', false);
                        try {
                            var response = JSON.parse(resp);
                        } catch(ex) {
                            response =  {
                                globalErrors: [resp]
                            }
                        }
                        if (response && (response.globalErrors || response.fieldErrors)) {
                            self.putErrors(response);
                        } else {
                            self.onClose();
                            self.model.set(response);
                            self.model.trigger('sync', self.model);
                        }
                    })
                    .fail(function(xhr){
                        var error;
                        try {
                            error = JSON.parse(xhr.responseText) || {};
                        } catch (ex) {
                            error = {
                                globalErrors: [xhr.statusText]
                            }
                        }
                        self.putErrors(error);
                    });
                }
            }
        },

        onInvalid: function(){
            this.putErrors(this.model.validationError);
        },

        /**
         * Validates field's value when it is changed
         * @param {Event} e
         */
        onFieldChanged: function(e){
            var $control = $(e.target),
                field = $control.attr('data-field'),
                value = $control.val().trim(),
                fields = {};
            fields[field] = value;
            if (field == 'file') {
                var fileName = value.match(/([^\\\/:]+)\.(csv|xml)$/i);
                if (fileName) {
                    fileName = fileName[1];
                    this.ui.name.val(fileName).change();
                }
            }
            var errors = this.model.validate(fields);
            this.putError(field, $control, errors);
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

        putErrors: function(errors){
            errors = errors || {};
            errors.globalErrors = errors.globalErrors || [];
            errors.fieldErrors = errors.fieldErrors || {};
            this.ui.alert.toggle(errors.globalErrors.length > 0)
                .toggleClass('error', errors.globalErrors.length > 0)
                .text( errors.globalErrors.join(', ') );
            this.ui.controls.each(function(){
                var $control = $(this),
                    field = $control.attr('data-field'),
                    msg = errors.fieldErrors[field];
                $control.closest('.control-group').toggleClass('error', !!msg)
                    .find('.help-block').text(msg || '');
            });
            this.ui.btnSave.attr('disabled', !_.isEmpty(errors.fieldErrors));
        },

        /**
         * Enables/disables button depending on errors presence
         */
        updateSaveBtn: function(){
            this.ui.btnSave.attr('disabled', this.hasInvalidFields());
        },

        hasInvalidFields: function(){
            return _.compact(_.values(this.invalidFields)).length > 0;
        },

        applyModelParams: function(){
            var fields = this.model.modelParams.fields;
            _.each(fields, function(field, fieldName){
                var $control = this.ui.controls.filter('[data-field="' + fieldName + '"]'),
                    $label = $control.closest('.control-group').children('label');
                $control.add($label).attr('required', !!field.required);
                if ($control.is('input') && field.maxLength) {
                    $control.attr('maxlength', +field.maxLength);
                }
            }, this);
        }
    });

    return DictionaryPopupUploadView;
});
