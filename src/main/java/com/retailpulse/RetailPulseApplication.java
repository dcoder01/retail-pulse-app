package com.retailpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RetailPulse Monolith
 *
 * A single RESTful Spring Boot application that consolidates:
 *   - Inventory Service  → /api/products/**
 *   - Order Service      → /api/orders/**
 *   - Notification Service → /api/notifications/**
 *
 * No Eureka, no API Gateway, no inter-service HTTP calls.
 * All communication is direct Java method calls within the same JVM.
 */
@SpringBootApplication
public class RetailPulseApplication {
    public static void main(String[] args) {
        SpringApplication.run(RetailPulseApplication.class, args);
    }
}
