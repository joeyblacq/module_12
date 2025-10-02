package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /** GET /api/products?restaurant={id} (SQL 5) */
    public List<Product> listByRestaurantId(int restaurantId) {
        return productRepository.findProductsByRestaurantIdNative(restaurantId);
    }

    /** DELETE /api/products?restaurant={id} (SQL 6) — returns number deleted */
    public int deleteByRestaurantId(int restaurantId) {
        return productRepository.deleteProductsByRestaurantIdNative(restaurantId);
    }
}
