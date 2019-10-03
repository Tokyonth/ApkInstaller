package com.tokyonth.installer.permissions;

import java.util.List;

public class PermInfo {

    private List<String> permissionGroup;
    private List<String> permissionLabel;
    private List<String> permissionDescription;

    public List<String> getPermissionGroup() {
        return permissionGroup;
    }

    public void setPermissionGroup(List<String> permissionGroup) {
        this.permissionGroup = permissionGroup;
    }

    public List<String> getPermissionLabel() {
        return permissionLabel;
    }

    public void setPermissionLabel(List<String> permissionLabel) {
        this.permissionLabel = permissionLabel;
    }

    public List<String> getPermissionDescription() {
        return permissionDescription;
    }

    public void setPermissionDescription(List<String> permissionDescription) {
        this.permissionDescription = permissionDescription;
    }

}
