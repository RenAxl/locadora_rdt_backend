package com.locadora_rdt_backend.modules.organization.suppliers.dto;

public class SupplierImageDTO {

    private byte[] image;
    private String contentType;

    public SupplierImageDTO(byte[] image, String contentType) {
        this.image = image;
        this.contentType = contentType;
    }

    public byte[] getImage() {
        return image;
    }

    public String getContentType() {
        return contentType;
    }
}
