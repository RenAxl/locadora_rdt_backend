package com.locadora_rdt_backend.modules.organization.customers.dto;

public class CustomerPhotoDTO {

    private byte[] photo;
    private String contentType;

    public CustomerPhotoDTO(byte[] photo, String contentType) {
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
