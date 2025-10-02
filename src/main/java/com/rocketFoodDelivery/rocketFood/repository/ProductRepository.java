package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    /* ===== SQL 5 (filtered) ===== */
    @Query(value = """
        SELECT *
          FROM products
         WHERE restaurant_id = :restaurantId
         ORDER BY id DESC
        """, nativeQuery = true)
    List<Product> findProductsByRestaurantIdNative(@Param("restaurantId") int restaurantId);

    /* ===== API 7 (unfiltered) ===== */
    @Query(value = """
        SELECT *
          FROM products
         ORDER BY id DESC
        """, nativeQuery = true)
    List<Product> findAllOrderByIdDescNative();

    /* ===== SQL 6 (DELETE by restaurant) ===== */
    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM products
         WHERE restaurant_id = :restaurantId
        """, nativeQuery = true)
    int deleteProductsByRestaurantId(@Param("restaurantId") int restaurantId);
}
