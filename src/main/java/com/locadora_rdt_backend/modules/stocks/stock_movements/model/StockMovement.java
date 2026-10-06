package com.locadora_rdt_backend.modules.stocks.stock_movements.model;

import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tb_stock_movement")
public class StockMovement implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockMovementType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_unit_id", foreignKey = @ForeignKey(name = "fk_stock_movement_item_unit"))
    private ItemUnit itemUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private ItemUnitStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 30)
    private ItemUnitStatus newStatus;

    public StockMovement() {
    }

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }

    public ItemUnit getItemUnit() {
        return itemUnit;
    }

    public void setItemUnit(ItemUnit itemUnit) {
        this.itemUnit = itemUnit;
    }

    public ItemUnitStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ItemUnitStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public ItemUnitStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ItemUnitStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public StockMovementType getType() {
        return type;
    }

    public void setType(StockMovementType type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StockMovement stockMovement = (StockMovement) o;
        return Objects.equals(id, stockMovement.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
