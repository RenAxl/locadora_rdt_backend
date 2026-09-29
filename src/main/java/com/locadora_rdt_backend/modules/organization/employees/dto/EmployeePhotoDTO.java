package com.locadora_rdt_backend.modules.organization.employees.dto;

public class EmployeePhotoDTO {

    private byte[] photo;
    private String contentType;

    public EmployeePhotoDTO(byte[] photo, String contentType) {
        this.photo = photo;
        this.contentType = contentType;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public String getContentType() {
        return contentType;
    }
}
