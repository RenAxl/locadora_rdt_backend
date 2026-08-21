package com.locadora_rdt_backend.modules.identity.users.dto;

public class UserPhotoDTO {

    private byte[] photo;
    private String contentType;

    public UserPhotoDTO(byte[] photo, String contentType) {
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
