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

    /** API 7 – GET /api/products (no filter) */
    public List<Product> listAll() {
        return productRepository.findAllOrderByIdDescNative();
    }

    /** SQL 5 – GET /api/products?restaurant={id} */
    public List<Product> listByRestaurantId(int restaurantId) {
        return productRepository.findProductsByRestaurantIdNative(restaurantId);
    }

    /** SQL 6 – DELETE /api/products?restaurant={id} — returns deleted row count */
    public int deleteByRestaurantId(int restaurantId) {
        return productRepository.deleteProductsByRestaurantId(restaurantId);
    }
}
