package com.locadora_rdt_backend.modules.organization.customers.dto;

import java.io.Serializable;

public class CustomerFileViewDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    private String contentType;
    private byte[] data;

    public CustomerFileViewDTO() {
    }

    public CustomerFileViewDTO(String fileName, String contentType, byte[] data) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
