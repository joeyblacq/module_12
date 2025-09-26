package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // GET /api/products?restaurant={id} (native)
    @Query(value = """
        SELECT *
          FROM products
         WHERE restaurant_id = :restaurantId
         ORDER BY id DESC
        """, nativeQuery = true)
    List<Product> findProductsByRestaurantId(@Param("restaurantId") int restaurantId);

    // DELETE /api/products?restaurant={id} (native)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE FROM products
         WHERE restaurant_id = :restaurantId
        """, nativeQuery = true)
    int deleteProductsByRestaurantId(@Param("restaurantId") int restaurantId);
}
