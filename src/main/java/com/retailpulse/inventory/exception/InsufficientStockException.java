package com.retailpulse.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productCode, int available, int requested) {
        super(String.format("Insufficient stock for %s: available=%d, requested=%d",
                productCode, available, requested));
    }
}
