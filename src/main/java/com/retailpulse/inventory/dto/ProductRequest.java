package com.retailpulse.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank(message = "Product code is required")
    String productCode,

    @NotBlank(message = "Name is required")
    String name,

    String description,

    @NotBlank(message = "Category is required")
    String category,

    @NotNull @Positive(message = "Price must be positive")
    BigDecimal price,

    @NotNull @Min(value = 0, message = "Stock cannot be negative")
    Integer stockQuantity,

    Integer lowStockThreshold
) {}
