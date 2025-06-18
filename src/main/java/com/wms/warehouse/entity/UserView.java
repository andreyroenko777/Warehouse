package com.wms.warehouse.entity;


public class UserView {
    private final User user;
    private final boolean canEdit;
    private final boolean canDelete;

    public UserView(User user, boolean canEdit, boolean canDelete) {
        this.user = user;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    public User getUser() {
        return user;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }
}
