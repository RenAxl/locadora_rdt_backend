package com.locadora_rdt_backend.modules.dashboard.model;

import java.io.Serializable;
import java.time.LocalDate;

public class DashboardDailyRental implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private String label;
    private Long quantity;

    public DashboardDailyRental() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
