package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findById(int id);

    @Query(value = "SELECT * FROM products WHERE restaurant_id = :restaurantId ORDER BY id DESC", nativeQuery = true)
    List<Product> findProductsByRestaurantId(@Param("restaurantId") int restaurantId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM products WHERE restaurant_id = :restaurantId", nativeQuery = true)
    void deleteProductsByRestaurantId(@Param("restaurantId") int restaurantId);
}
