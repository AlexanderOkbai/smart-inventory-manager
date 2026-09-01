package com.smartinventory.dto;

import com.smartinventory.entity.MovementType;

import java.time.LocalDateTime;

public class StockMovementResponse {

    private Long id;
    private MovementType type;
    private Integer quantity;
    private LocalDateTime createdAt;

    public StockMovementResponse(
            Long id,
            MovementType type,
            Integer quantity,
            LocalDateTime createdAt) {

        this.id = id;
        this.type = type;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public MovementType getType() {
        return type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}