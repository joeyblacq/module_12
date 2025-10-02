package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // === Compatibility for DataSeeder ===
    @Query(value = """
        SELECT *
          FROM products
         WHERE restaurant_id = :restaurantId
         ORDER BY id DESC
        """, nativeQuery = true)
    List<Product> findByRestaurantId(@Param("restaurantId") int restaurantId);

    // === Our explicit native methods ===
    @Query(value = """
        SELECT *
          FROM products
         WHERE restaurant_id = :restaurantId
         ORDER BY id DESC
        """, nativeQuery = true)
    List<Product> findProductsByRestaurantIdNative(@Param("restaurantId") int restaurantId);

    @Query(value = """
        SELECT *
          FROM products
         ORDER BY id DESC
        """, nativeQuery = true)
    List<Product> findAllOrderByIdDescNative();

    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM products
         WHERE restaurant_id = :restaurantId
        """, nativeQuery = true)
    int deleteProductsByRestaurantId(@Param("restaurantId") int restaurantId);
}
