package com.locadora_rdt_backend.modules.identity.roles.dto;

import com.locadora_rdt_backend.modules.identity.roles.constants.RoleConstants;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class RolePermissionsUpdateDTO {

    @NotEmpty(message = RoleConstants.PERMISSIONS_REQUIRED)
    private List<Long> permissionIds = new ArrayList<>();

    public RolePermissionsUpdateDTO() {
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }
    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
