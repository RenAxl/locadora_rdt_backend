package com.locadora_rdt_backend.modules.identity.permissions.dto;

import com.locadora_rdt_backend.modules.identity.permissions.model.Permission;

import java.io.Serializable;

public class PermissionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String groupName;

    public PermissionDTO() {
    }

    public PermissionDTO(Long id, String name, String groupName) {
        this.id = id;
        this.name = name;
        this.groupName = groupName;
    }

    public PermissionDTO(Permission entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.groupName = entity.getGroupName();
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
