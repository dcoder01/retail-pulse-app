package com.retailpulse.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockReserveRequest(
    @NotNull @Min(value = 1, message = "Reserve quantity must be at least 1")
    Integer quantity
) {}
