package com.retailpulse.inventory.dto;

import com.retailpulse.inventory.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long          id,
    String        productCode,
    String        name,
    String        description,
    String        category,
    BigDecimal    price,
    Integer       stockQuantity,
    Integer       lowStockThreshold,
    boolean       lowStock,
    LocalDateTime updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
            p.getId(), p.getProductCode(), p.getName(), p.getDescription(),
            p.getCategory(), p.getPrice(), p.getStockQuantity(),
            p.getLowStockThreshold(),
            p.getStockQuantity() <= p.getLowStockThreshold(),
            p.getUpdatedAt()
        );
    }
}
