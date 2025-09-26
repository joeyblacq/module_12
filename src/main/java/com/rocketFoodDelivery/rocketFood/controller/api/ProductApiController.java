package com.rocketFoodDelivery.rocketFood.controller;

import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductApiController {

    private final ProductRepository productRepository;

    public ProductApiController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // GET /api/products  and  /api/products?restaurant=ID
    @GetMapping
    public ResponseEntity<List<Product>> list(@RequestParam(value = "restaurant", required = false) Integer restaurantId) {
        if (restaurantId != null) {
            return ResponseEntity.ok(productRepository.findByRestaurantId(restaurantId));
        }
        return ResponseEntity.ok(productRepository.findAllOrderByIdDesc());
    }
}
