package com.retailpulse.order.dto;

import jakarta.validation.constraints.*;

public record OrderRequest(
    @NotBlank(message = "Customer ID is required")
    String customerId,

    @NotBlank(message = "Product code is required")
    String productCode,

    @NotNull @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {}
