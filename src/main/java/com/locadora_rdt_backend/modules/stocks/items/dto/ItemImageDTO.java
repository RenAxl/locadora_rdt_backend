package com.locadora_rdt_backend.modules.stocks.items.dto;

public class ItemImageDTO {

    private byte[] image;
    private String contentType;

    public ItemImageDTO(byte[] image, String contentType) {
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
