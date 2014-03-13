'use strict';

define([
    'underscore',
    'jquery',
    'routes',
    'view/item-view',
    'view/view-helpers',
    'text!template/groups/group.html',
    'text!template/groups/group-delete.html'
], function (_, $, routes, ItemView, ViewHelpers, viewTemplate, deleteTemplate) {

    /**
     * Individual group widget.
     * @class
     */
    var GroupView = ItemView.extend({
        className: 'tile',
        template: _.template(viewTemplate),
        templateHelpers: function () {
            var self = this;
            return _.extend({
                getMode: function () {
                    return self.mode;
                }
            }, ViewHelpers.templateHelpers);
        },
        events: {
            'mousedown [data-mode]': 'onSwitchModeClick',
            'mousedown [data-action="delete"]': 'onDeleteClick',
            'mousedown [data-action="undo"]': 'onUndoMousedown',
            'keydown [data-field]': 'onEditorKeydown',
            'blur [data-field]': 'onEditorBlur',
            'click [data-action="destroy"]': 'onDestroyClick',
            'click [data-action="cancel"]': 'onCancelClick',
            'keydown .delete-dialog': 'onDialogKeydown'
        },
        modelEvents: {
            'change': 'render'
        },
        ui: {
            'article': 'article',
            'editor': '[data-field]',
            'edit': 'span.edit',
            'header': 'header',
            'name': '[data-label="name"]'
        },
        onShow: function(){
            $(window).on('resize.' + this.cid, _.bind(this.onResize, this));
            this.onResize();
        },
        onClose: function(){
            $(window).off('resize.' + this.cid);
        },
        onResize: function(){
            this.updateButtonsPosition();
        },
        onRender: function () {
            this.prepareEditor();
            this.updateButtonsPosition();
        },

        prepareEditor: function(){
            var $editor = this.ui.editor,
                field = $editor.attr('data-field'),
                fields = this.model.modelParams.fields;
            if (fields && fields[field] && fields[field].maxLength) {
                $editor.attr('maxlength', fields[field].maxLength);
            }
            $editor.focus().val($editor.val());
        },
        updateButtonsPosition: function(){
            var hw = this.ui.header.width(),
                aw = this.ui.name.outerWidth(true),
                nameLeft = parseFloat(this.ui.header.css('paddingLeft')) + this.ui.header.position().left,
                nameRight = 8;
            this.ui.edit.css({ left: Math.min(hw, aw) + nameLeft + nameRight });
        },
        switchMode: function(mode){
            if (this.model.isReadonly()) {
                return;
            }
            if (mode != this.mode) {
                this.mode = mode;
                this.undelegateEvents();
                this.render();
                this.delegateEvents();
            }
        },
        onSwitchModeClick: function (e) {
            var mode = $(e.target).attr('data-mode');
            this.switchMode(mode);
            e.preventDefault();
        },
        applyEditing: function () {
            var field = this.ui.editor.attr('data-field'),
                value = this.ui.editor.val().trim();
            this.model.safeSet(field, value);
            this.cancelEditing();
        },
        cancelEditing: function () {
            this.switchMode();
        },

        onUndoMousedown: function (e) {
            this.cancelEditing();
        },
        onEditorKeydown: function (e) {
            var key = e.keyCode || e.which;
            switch (key) {
                case 13:
                    this.applyEditing();
                    break;
                case 27:
                    this.cancelEditing();
                    break;
            }
        },
        onEditorBlur: function (e) {
            this.applyEditing();
        },
        onDeleteClick: function () {
            this.switchMode();
            this.addDeleteDialog();
        },
        removeDeleteDialog: function(){
            this.$('.delete-dialog').fadeOut(function(){
                $(this).dequeue().remove();
            })
        },
        addDeleteDialog: function(){
            var $dialog = $(_.template(deleteTemplate, ViewHelpers.templateHelpers));
            this.removeDeleteDialog();
            this.ui.article.append($dialog);
            $dialog.fadeIn(function(){
                $(this).dequeue().find('[autofocus]').focus();
            });
        },
        onCancelClick: function(){
            this.removeDeleteDialog();
        },
        onDestroyClick: function () {
            var xhr = this.model.destroy({
                // prevent firing 'remove' on this.groupsList
                silent: true,
                wait: true
            });
            if (xhr) {
                var collection = this.model.collection,
                    self = this;
                xhr.done(function () {
                        self.undelegateEvents();
                        self.$el.fadeOut(function () {
                            $(this).dequeue();
                            collection.trigger('remove', self.model);
                        });
                    })
                    .always(function(){
                        self.removeDeleteDialog();
                    })
            }
        },
        onDialogKeydown: function(e){
            var key = e.keyCode || e.which;
            switch (key) {
                case 27:
                    this.removeDeleteDialog();
                    break;
                case 9:
                    e.preventDefault();
                    this.$('.buttons button:not(:focus)').focus();
            }
        }

    });

    return GroupView;

});
