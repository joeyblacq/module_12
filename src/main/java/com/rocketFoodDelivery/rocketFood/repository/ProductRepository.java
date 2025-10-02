package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    /* ===============================
       NATIVE READ for SQL 5 (GET)
       =============================== */
    @Query(value = """
        SELECT *
          FROM products
         WHERE restaurant_id = :restaurantId
         ORDER BY id DESC
        """, nativeQuery = true)
    List<Product> findProductsByRestaurantIdNative(@Param("restaurantId") int restaurantId);

    /* =================================
       NATIVE DELETE for SQL 6 (DELETE)
       ================================= */
    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM products
         WHERE restaurant_id = :restaurantId
        """, nativeQuery = true)
    int deleteProductsByRestaurantIdNative(@Param("restaurantId") int restaurantId);
}
