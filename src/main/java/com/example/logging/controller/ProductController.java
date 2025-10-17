package com.example.logging.controller;

import com.example.logging.entity.Product;
import com.example.logging.repository.ProductRepository;
import com.example.logging.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        if (product.getPrice() < 0) {
            Map<String, Object> context = new HashMap<>();
            context.put("product_name", product.getName());
            context.put("price", product.getPrice());
            LoggerUtil.logError(logger, "Invalid price for product", new IllegalArgumentException("Price cannot be negative"), context);
            return ResponseEntity.badRequest().body("Price cannot be negative");
        }
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    return ResponseEntity.ok(productRepository.save(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/order")
    public ResponseEntity<?> orderProduct(@PathVariable Long id, @RequestBody Map<String, Integer> orderRequest) {
        long startTime = System.currentTimeMillis();
        Integer quantity = orderRequest.get("quantity");

        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be positive");
        }

        return productRepository.findById(id)
                .map(product -> {
                    double totalPrice = product.getPrice() * quantity;
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("product_id", id);
                    logData.put("product_name", product.getName());
                    logData.put("quantity", quantity);
                    logData.put("total_price", totalPrice);
                    logData.put("duration_ms", System.currentTimeMillis() - startTime);

                    LoggerUtil.logInfo(logger, "Product ordered successfully", logData);

                    return ResponseEntity.ok(Map.of("message", "Order successful", "totalPrice", totalPrice));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
