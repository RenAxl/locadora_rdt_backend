package com.locadora_rdt_backend.modules.identity.roles.dto;

import com.locadora_rdt_backend.modules.identity.permissions.dto.PermissionDTO;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RoleDetailsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String authority;
    private Long permissionsCount;

    private List<PermissionDTO> permissions = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    public RoleDetailsDTO() {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}