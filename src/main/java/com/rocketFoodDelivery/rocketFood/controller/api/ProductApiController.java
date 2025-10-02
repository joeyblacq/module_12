package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * API 7 – GET /api/products
     * When no query params are provided, return ALL products (newest first).
     * If ?restaurant={id} is provided, return products for that restaurant (SQL 5).
     */
    @GetMapping
    public ResponseEntity<List<Product>> list(
            @RequestParam(value = "restaurant", required = false) Integer restaurantId) {

        List<Product> products = (restaurantId == null)
                ? productService.listAll()
                : productService.listByRestaurantId(restaurantId);

        return ResponseEntity.ok(products);
    }

    /**
     * SQL 6 – DELETE /api/products?restaurant={id}
     * (Keep your existing DELETE endpoint if already present elsewhere)
     */
    // @DeleteMapping
    // public ResponseEntity<Void> deleteByRestaurant(@RequestParam("restaurant") int restaurantId) { ... }
}
