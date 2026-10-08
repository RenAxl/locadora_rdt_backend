package com.locadora_rdt_backend.modules.rentals.rental.dto;

import java.io.Serializable;

public class ItemAvailabilityDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private String itemName;
    private Long availableQuantity;
    private Long reservedQuantity;
    private Long rentedQuantity;

    public ItemAvailabilityDTO() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Long getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Long availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Long getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Long reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Long getRentedQuantity() {
        return rentedQuantity;
    }

    public void setRentedQuantity(Long rentedQuantity) {
        this.rentedQuantity = rentedQuantity;
    }
}
