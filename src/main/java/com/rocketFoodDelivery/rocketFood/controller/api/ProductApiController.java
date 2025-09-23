// ===================== ProductApiController.java =====================
package com.rocketFoodDelivery.rocketFood.controller;

import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductApiController {
    private final ProductRepository productRepository;

    public ProductApiController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // GET /api/products  (supports ?restaurant={id})
    @GetMapping("/api/products")
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(name = "restaurant", required = false) Integer restaurantId
    ) {
        if (restaurantId != null) {
            return ResponseEntity.ok(productRepository.findProductsByRestaurantId(restaurantId));
        }
        // fallback: all products
        return ResponseEntity.ok(productRepository.findAll());
    }
}

