'use strict';

define([
    'jquery',
    'underscore',
    'locale/i18n',
    'view/item-view',
    'text!template/groups/group-popup-add-view.html'
], function ($, _, i18n, ItemView, editTemplate) {

    var GroupPopupAddView = ItemView.extend({
        className: 'modal fade in',
        attributes: {style: 'display:none'},

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
            controls: '[data-field]',
            btnSave: '[data-action="submit"]'
        },
        onShow: function(){
            this.$el.modal('show');
        },
        onClose: function(){
            this.$el.modal('hide');
        },
        onRender: function(){
            this.setInvalidFields();
            this.updateSaveBtn();
            this.applyModelParams();
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

        /**
         * Shows errors if model changing failed
         */
        onInvalid: function(){
            this.putErrors(this.model.validationError);
        },

        onSubmit: function(){
            var attrs = this.getFieldsValues();
            if (this.model.set(attrs)) {
                var self = this,
                    errorsHandler = _.bind(this.putErrors, this),
                    xhr = this.model.save(attrs,{
                        wait: true,
                        fieldErrorsHandler: errorsHandler,
                        globalErrorsHandler: errorsHandler,
                        disableControls: this.ui.btnSave
                    });
                    if (xhr) {
                        xhr.done(function(){
                            self.onClose();
                        });
                    }
            }
        },

        /**
         * Validates field's value when it is changed
         * @param {Event} e
         */
        onFieldChanged: function(e){
            var $control = $(e.target),
                field = $control.attr('data-field'),
                fields = {};
            fields[field] = $control.val().trim();
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

        /**
         * Enables/disables button depending on errors presence
         */
        updateSaveBtn: function(){
            this.ui.btnSave.attr('disabled', this.hasInvalidFields());
        },

        hasInvalidFields: function(){
            return _.compact(_.values(this.invalidFields)).length > 0;
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

    return GroupPopupAddView;
});
