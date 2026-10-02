package com.locadora_rdt_backend.modules.reports.financial_reports.dto;

import java.io.Serializable;

public class FinancialReportFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    private String contentType;
    private byte[] data;

    public FinancialReportFileDTO() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
