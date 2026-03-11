package com.retailpulse.inventory.service;

import com.retailpulse.inventory.dto.*;
import com.retailpulse.inventory.exception.*;
import com.retailpulse.inventory.model.Product;
import com.retailpulse.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse getByCode(String productCode) {
        return ProductResponse.from(findByCode(productCode));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .productCode(request.productCode())
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .lowStockThreshold(request.lowStockThreshold() != null ? request.lowStockThreshold() : 10)
                .build();

        product = productRepository.save(product);
        log.info("Created product: {}", product.getProductCode());
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse updateStock(String productCode, int quantity) {
        Product product = findByCode(productCode);
        int previous = product.getStockQuantity();
        product.setStockQuantity(quantity);
        productRepository.save(product);
        log.info("Stock updated for {}: {} -> {}", productCode, previous, quantity);
        if (quantity <= product.getLowStockThreshold()) {
            log.warn("LOW STOCK: {} has {} units (threshold: {})",
                    productCode, quantity, product.getLowStockThreshold());
        }
        return ProductResponse.from(product);
    }

    /**
     * Atomically reserve (subtract) stock.
     * Called directly by OrderService — no HTTP hop.
     */
    @Transactional
    public ProductResponse reserveStock(String productCode, int quantity) {
        Product product = findByCode(productCode);

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(productCode, product.getStockQuantity(), quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
        log.info("Reserved {} units of {} | Remaining: {}", quantity, productCode, product.getStockQuantity());
        if (product.getStockQuantity() <= product.getLowStockThreshold()) {
            log.warn("LOW STOCK after reserve: {} has {} units", productCode, product.getStockQuantity());
        }
        return ProductResponse.from(product);
    }

    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<ProductResponse> getByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(ProductResponse::from)
                .toList();
    }

    // ── Private helper ────────────────────────────────────────────
    private Product findByCode(String productCode) {
        return productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException(productCode));
    }
}
