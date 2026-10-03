package com.locadora_rdt_backend.modules.stocks.categories.dto;

public class CategoryImageDTO {

    private byte[] image;
    private String contentType;

    public CategoryImageDTO(byte[] image, String contentType) {
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
