package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // existing
    @Query(value = """
        SELECT *
          FROM products
         WHERE restaurant_id = :restaurantId
         ORDER BY id
        """, nativeQuery = true)
    List<Product> findByRestaurantId(@Param("restaurantId") int restaurantId);

    // NEW: matches your call site (Integer param + method name)
    @Query(value = """
        SELECT *
          FROM products
         WHERE restaurant_id = :restaurantId
         ORDER BY id
        """, nativeQuery = true)
    List<Product> findProductsByRestaurantId(@Param("restaurantId") Integer restaurantId);

    // existing (if you have it)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE FROM products
         WHERE restaurant_id = :restaurantId
        """, nativeQuery = true)
    int deleteProductsByRestaurantId(@Param("restaurantId") int restaurantId);
}
