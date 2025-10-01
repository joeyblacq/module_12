package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Standard Spring Data derived query
    List<Product> findByRestaurantId(Integer restaurantId);

    // Alias to satisfy existing code that calls findProductsByRestaurantId(...)
    @Query("SELECT p FROM Product p WHERE p.restaurant.id = :restaurantId")
    List<Product> findProductsByRestaurantId(@Param("restaurantId") Integer restaurantId);
    List<Product> deleteProductsByRestaurantId(@Param("restaurantId") Integer restaurantId);
}
