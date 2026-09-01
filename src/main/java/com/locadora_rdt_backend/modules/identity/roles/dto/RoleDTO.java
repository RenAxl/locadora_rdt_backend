package com.locadora_rdt_backend.modules.identity.roles.dto;

import com.locadora_rdt_backend.modules.identity.permissions.dto.PermissionDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String authority;
    private Long permissionsCount;

    private List<PermissionDTO> permissions = new ArrayList<>();

    public RoleDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Long getPermissionsCount() {
        return permissionsCount;
    }

    public void setPermissionsCount(Long permissionsCount) {
        this.permissionsCount = permissionsCount;
    }

    public List<PermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionDTO> permissions) {
        this.permissions = permissions;
    }

}