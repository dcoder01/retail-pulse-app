package com.retailpulse.order.controller;

import com.retailpulse.common.ApiResponse;
import com.retailpulse.order.dto.*;
import com.retailpulse.order.model.OrderStatus;
import com.retailpulse.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest request) {

        OrderResponse order = orderService.placeOrder(request);

        HttpStatus status = switch (order.status()) {
            case CONFIRMED -> HttpStatus.CREATED;
            case FAILED    -> HttpStatus.UNPROCESSABLE_ENTITY;
            default        -> HttpStatus.ACCEPTED;
        };

        return ResponseEntity.status(status)
                .body(ApiResponse.ok(order.status().name().toLowerCase(), order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(id)));
    }

    @GetMapping("/ref/{orderRef}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByRef(@PathVariable String orderRef) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderByRef(orderRef)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders(status, customerId)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderStats()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "module", "order"));
    }
}
