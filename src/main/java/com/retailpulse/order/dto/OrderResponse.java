package com.retailpulse.order.dto;

import com.retailpulse.order.model.Order;
import com.retailpulse.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
    Long        id,
    String      orderRef,
    String      customerId,
    String      productCode,
    Integer     quantity,
    BigDecimal  unitPrice,
    BigDecimal  totalAmount,
    OrderStatus status,
    String      failureReason,
    LocalDateTime createdAt
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(
            o.getId(), o.getOrderRef(), o.getCustomerId(), o.getProductCode(),
            o.getQuantity(), o.getUnitPrice(), o.getTotalAmount(),
            o.getStatus(), o.getFailureReason(), o.getCreatedAt()
        );
    }
}
