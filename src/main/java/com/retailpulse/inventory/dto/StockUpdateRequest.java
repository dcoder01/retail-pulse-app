package com.retailpulse.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
    @NotNull @Min(value = 0, message = "Quantity cannot be negative")
    Integer quantity
) {}
