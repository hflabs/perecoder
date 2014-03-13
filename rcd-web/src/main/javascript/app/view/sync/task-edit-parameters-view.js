'use strict';

define(function(require){
    var _ = require('underscore'),
        $ = require('jquery'),
        i18n = require('locale/i18n'),
        ItemView = require('view/item-view'),
        ViewHelpers = require('view/view-helpers');

    var TEMPLATES = {
        'dummyTaskPerformer': require('text!template/sync/task-descriptor-parameters-dummy-view.html'),
        'cnsiSyncTaskPerformer': require('text!template/sync/task-descriptor-parameters-cnsi-view.html'),
        'dataSourceSyncTaskPerformer': require('text!template/sync/task-descriptor-parameters-db-view.html'),
        'indexRebuildTaskPerformer': require('text!template/sync/task-descriptor-parameters-index-view.html')
    };

    var defaultHint = {
        'parameters.url': 'http://server:port/service'
    };

    /**
     * @class 
     */
    var TaskEditParametersView = ItemView.extend({
        className: 'modal fade in',
        attributes: {style: 'display:none'},
        templateHelpers: function(){
            var self = this;
            return _.extend({
                getDefaultHint: function(parameter){
                    return defaultHint[parameter] || '';
                },
                getAvailableValues: function(){
                    return self.model.task_type.availableValues;
                }
            }, ViewHelpers.templateHelpers);
        },
        events: {
            'switch-change .make-switch': 'onSwitchChanged',
            'click [data-action="save"]': 'applyEdit',
            'change [data-field]': 'onFieldChanged',
            'keyup [data-field]': 'onFieldChanged'
        },
        initialize: function(){
            var template = TEMPLATES[this.model.get('name')];
            this.template = _.template(template || 'Template not defined');
            this.listenTo(this.model, 'invalid', this.onInvalid);
        },
        ui: {
            alert: '.alert',
            btnSave: '[data-action="save"]',
            controls: '[data-field]',
            switches: '.make-switch',
            selects: '.selectpicker'
        },
        onRender: function(){
            this.ui.switches.not('.has-switch').bootstrapSwitch();
            this.ui.selects.selectpicker();
            this.applyConfig();
            this.ui.selects.change();
            this.updateDefaults();
        },
        onShow: function(){
            this.$el.modal('show');
            this.resetInputsSelection();
        },
        onClose: function(){
            this.$el.modal('hide');
        },

        /**
         * Puts 'required', 'maxlength', 'min' and 'max' attributes to INPUTs
         */
        applyConfig: function(){
            var fields = this.model.task_type.fields;
            this.ui.controls.filter('input').each(function(){
                var $control = $(this),
                    field = $control.attr('data-field'),
                    fieldData = fields[field];
                if (!fieldData) {
                    return;
                }
                $control.add($control.closest('.control-group').find('label'))
                    .attr('required', !!fieldData.required);
                switch (fieldData.type) {
                    case 'STRING':
                        if (fieldData.maxLength != null) {
                            $control.attr('maxlength', fieldData.maxLength);
                        }
                        break;
                    case 'NUMBER':
                        if (fieldData.minLength != null) {
                            $control.attr('min', fieldData.minLength);
                        }
                        if (fieldData.maxLength != null) {
                            $control.attr('max', fieldData.maxLength);
                        }
                        break;
                }
            });
        },
        resetInputsSelection: function(){
            this.ui.controls.each(function(){
                this.value = this.value;
            });
        },
        onFieldChanged: function(e){
            this.updateDefaults(e);
            var $control = $(e.target);
            var newAttrs = this.readControlsData($control);
            var error = this.model.validate(newAttrs);
            this.putErrors(error);
        },
        updateDefaults: function(e){
            var $driverName = this.ui.controls.filter('[data-field="parameters.driverName"]');
            if ($driverName.length && (!e || $(e.target).is($driverName))) {
                var driverName = $driverName.val(),
                    driverNames = this.model.task_type.availableValues.driverName,
                    $jdbcUrl = this.ui.controls.filter('[data-field="parameters.jdbcUrl"]'),
                    jdbcUrl = $jdbcUrl.val().trim();
                if (driverName) {
                    var isUrlUnchanged = !jdbcUrl || _.any(driverNames, function(driver) {
                        return driver.urlTemplate.indexOf(jdbcUrl) == 0;
                    });
                    var urlTemplate = driverNames[driverName] && driverNames[driverName].urlTemplate;
                    if (urlTemplate && isUrlUnchanged) {
                        $jdbcUrl.val(urlTemplate);
                    }
                }
            }
        },
        readControlsData: function($controls){
            var attributes = {},//_.clone(this.model.attributes, true),
                attrHolder;
            $controls.each(function(){
                var $control = $(this),
                    field = $control.attr('data-field').split('.'),
                    value = $control.is(':checkbox') ? !!$control.attr('checked') : $control.val().trim();
                attrHolder = attributes;
                if (field.length) {
                    while (field.length > 1) {
                        var ns = field.shift();
                        attrHolder = attrHolder[ns] || (attrHolder[ns] = {});
                    }
                    attrHolder[field[0]] = value;
                }
            });
            return attributes;
        },
        onSwitchChanged: function(e, data) {
            $(e.target).find(':checkbox').attr('checked', data.value);
        },

        applyEdit: function(){
            var newAttrs = this.readControlsData(this.ui.controls),
                errors = this.model.validate(newAttrs);
            if (errors) {
                this.putErrors(errors);
            } else {
                // New attributes are valid, so just put them into the model
                $.extend(true, this.model.attributes, newAttrs);
                var errorsHandler = _.bind(this.putErrors, this),
                    xhr = this.model.save(null, {
                        wait: true,
                        fieldErrorsHandler: errorsHandler,
                        globalErrorsHandler: errorsHandler,
                        disableControls: this.ui.btnSave
                    });
                if (xhr) {
                    var self = this;
                    xhr.done(function(){
                        self.onClose();
                    });
                }
            }
        },
        
        onInvalid: function(){
            this.putErrors(this.model.validationError);
        },

        putErrors: function(errors){
            errors = errors || {};
            errors.globalErrors = errors.globalErrors || [];
            errors.fieldErrors = errors.fieldErrors || {};
            this.ui.alert.toggle(errors.globalErrors.length > 0)
                .toggleClass('alert-error', errors.globalErrors.length > 0)
                .text( errors.globalErrors.join(', ') );
            this.ui.controls.each(function(){
                var $control = $(this),
                    field = $control.attr('data-field'),
                    msg = errors.fieldErrors[field];
                $control.closest('.control-group').toggleClass('error', !!msg)
                    .find('.help-block').text(msg || defaultHint[field] || '');
            });
            this.ui.btnSave.attr('disabled', !_.isEmpty(errors.fieldErrors));
        }
        
    });
    
    return TaskEditParametersView;
});
