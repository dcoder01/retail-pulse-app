package com.retailpulse.inventory.controller;

import com.retailpulse.common.ApiResponse;
import com.retailpulse.inventory.dto.*;
import com.retailpulse.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String category) {
        List<ProductResponse> products = category != null
                ? inventoryService.getByCategory(category)
                : inventoryService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/{productCode}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable String productCode) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getByCode(productCode)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created", inventoryService.createProduct(request)));
    }

    @PutMapping("/{productCode}/stock")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable String productCode,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Stock updated", inventoryService.updateStock(productCode, request.quantity())));
    }

    @PostMapping("/{productCode}/reserve")
    public ResponseEntity<ApiResponse<ProductResponse>> reserveStock(
            @PathVariable String productCode,
            @Valid @RequestBody StockReserveRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Stock reserved", inventoryService.reserveStock(productCode, request.quantity())));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLowStockProducts()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "module", "inventory"));
    }
}
