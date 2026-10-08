package com.locadora_rdt_backend.modules.rentals.rental.dto;

import java.io.Serializable;
import java.time.Instant;

public class RentalStatusHistoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String previousStatus;
    private String newStatus;
    private String reason;
    private Instant changedAt;
    private String changedBy;

    public RentalStatusHistoryDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
}
