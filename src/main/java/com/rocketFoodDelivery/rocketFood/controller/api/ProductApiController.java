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
     * SQL 5 – GET /api/products?restaurant={id}
     * Native SQL in repository.
     */
    @GetMapping
    public ResponseEntity<List<Product>> findByRestaurant(@RequestParam("restaurant") int restaurantId) {
        List<Product> products = productService.listByRestaurantId(restaurantId);
        return ResponseEntity.ok(products);
    }

    /**
     * SQL 6 – DELETE /api/products?restaurant={id}
     * Native SQL in repository. Returns 204 with X-Deleted-Count header.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteByRestaurant(@RequestParam("restaurant") int restaurantId) {
        int deleted = productService.deleteByRestaurantId(restaurantId);
        return ResponseEntity.noContent()
                .header("X-Deleted-Count", String.valueOf(deleted))
                .build();
    }
}
