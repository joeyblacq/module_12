// src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/ProductsApiController.java
package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductsApiController {

    private final ProductRepository productRepository;

    public ProductsApiController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<Product>> list(@RequestParam(value = "restaurant", required = false) Integer restaurantId) {
        if (restaurantId != null) {
            return ResponseEntity.ok(productRepository.findProductsByRestaurantId(restaurantId));
        }
        var all = productRepository.findAll();
        all.sort(Comparator.comparingInt(Product::getId).reversed());
        return ResponseEntity.ok(all);
    }
}
