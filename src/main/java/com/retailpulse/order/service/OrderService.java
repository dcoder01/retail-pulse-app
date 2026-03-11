package com.retailpulse.order.service;

import com.retailpulse.inventory.dto.ProductResponse;
import com.retailpulse.inventory.exception.InsufficientStockException;
import com.retailpulse.inventory.exception.ProductNotFoundException;
import com.retailpulse.inventory.service.InventoryService;
import com.retailpulse.notification.service.NotificationService;
import com.retailpulse.order.dto.*;
import com.retailpulse.order.model.*;
import com.retailpulse.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Handles order placement by directly invoking InventoryService and
 * NotificationService as Spring beans — no HTTP, no Feign, no RestTemplate.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository     orderRepository;
    private final InventoryService    inventoryService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
                        InventoryService inventoryService,
                        NotificationService notificationService) {
        this.orderRepository     = orderRepository;
        this.inventoryService    = inventoryService;
        this.notificationService = notificationService;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {

        String orderRef = generateOrderRef();
        log.info("Processing order {} for customer {} — product {} x{}",
                orderRef, request.customerId(), request.productCode(), request.quantity());

        // ── Step 1: Fetch product details (direct Java call) ───────────
        ProductResponse product;
        try {
            product = inventoryService.getByCode(request.productCode());
        } catch (ProductNotFoundException ex) {
            return saveFailedOrder(orderRef, request, null,
                    "Product not found: " + request.productCode());
        }

        BigDecimal unitPrice    = product.price();
        BigDecimal totalAmount  = unitPrice.multiply(BigDecimal.valueOf(request.quantity()));

        // ── Step 2: Reserve stock (direct Java call) ───────────────────
        try {
            inventoryService.reserveStock(request.productCode(), request.quantity());
        } catch (InsufficientStockException ex) {
            return saveFailedOrder(orderRef, request, unitPrice,
                    "Insufficient stock: " + ex.getMessage());
        }

        // ── Step 3: Persist confirmed order ───────────────────────────
        Order order = Order.builder()
                .orderRef(orderRef)
                .customerId(request.customerId())
                .productCode(request.productCode())
                .quantity(request.quantity())
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .status(OrderStatus.CONFIRMED)
                .build();

        order = orderRepository.save(order);
        log.info("Order {} confirmed — total {}", orderRef, totalAmount);

        // ── Step 4: Notify (non-blocking — failure does NOT roll back) ─
        try {
            notificationService.sendOrderConfirmation(
                    orderRef, request.customerId(), request.productCode(), totalAmount.doubleValue());
        } catch (Exception ex) {
            log.warn("Notification failed for order {} (non-critical): {}", orderRef, ex.getMessage());
        }

        return OrderResponse.from(order);
    }

    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
    }

    public OrderResponse getOrderByRef(String orderRef) {
        return orderRepository.findByOrderRef(orderRef)
                .map(OrderResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderRef));
    }

    public List<OrderResponse> getAllOrders(String status, String customerId) {
        if (status != null) {
            OrderStatus os;
            try {
                os = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unknown order status: " + status);
            }
            return orderRepository.findByStatus(os).stream().map(OrderResponse::from).toList();
        }
        if (customerId != null) {
            return orderRepository.findByCustomerId(customerId).stream()
                    .map(OrderResponse::from).toList();
        }
        return orderRepository.findAll().stream().map(OrderResponse::from).toList();
    }

    public Map<String, Long> getOrderStats() {
        return Map.of(
            "total",     orderRepository.count(),
            "confirmed", orderRepository.countByStatus(OrderStatus.CONFIRMED),
            "pending",   orderRepository.countByStatus(OrderStatus.PENDING),
            "failed",    orderRepository.countByStatus(OrderStatus.FAILED)
        );
    }

    // ── Private helpers ────────────────────────────────────────────

    private String generateOrderRef() {
        String date = LocalDate.now().toString().replace("-", "");
        String seq  = String.format("%06d", (int) (Math.random() * 1_000_000));
        return "ORD-" + date + "-" + seq;
    }

    private OrderResponse saveFailedOrder(String orderRef, OrderRequest req,
                                           BigDecimal unitPrice, String reason) {
        BigDecimal price = (unitPrice != null) ? unitPrice : BigDecimal.ZERO;

        Order order = Order.builder()
                .orderRef(orderRef)
                .customerId(req.customerId())
                .productCode(req.productCode())
                .quantity(req.quantity())
                .unitPrice(price)
                .totalAmount(price.multiply(BigDecimal.valueOf(req.quantity())))
                .status(OrderStatus.FAILED)
                .failureReason(reason)
                .build();

        order = orderRepository.save(order);
        log.warn("Order {} failed: {}", orderRef, reason);

        try {
            notificationService.sendOrderFailure(orderRef, req.customerId(), reason);
        } catch (Exception ex) {
            log.warn("Failure notification skipped for {}: {}", orderRef, ex.getMessage());
        }

        return OrderResponse.from(order);
    }
}
