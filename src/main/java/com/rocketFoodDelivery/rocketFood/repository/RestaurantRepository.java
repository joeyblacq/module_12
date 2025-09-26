package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {

    /* =========
       ANALYTICS
       ========= */
    @Query(value = """
        SELECT r.id,
               r.name,
               r.price_range,
               AVG(o.restaurant_rating) AS avg_rating
          FROM restaurants r
          LEFT JOIN orders o ON o.restaurant_id = r.id
         WHERE r.id = :id
         GROUP BY r.id, r.name, r.price_range
        """, nativeQuery = true)
    List<Object[]> findRestaurantWithAverageRatingById(@Param("id") int id);

    @Query(value = """
        SELECT r.id,
               r.name,
               r.price_range,
               AVG(o.restaurant_rating) AS avg_rating
          FROM restaurants r
          LEFT JOIN orders o ON o.restaurant_id = r.id
         GROUP BY r.id, r.name, r.price_range
        HAVING (:rating IS NULL OR CEILING(COALESCE(AVG(o.restaurant_rating),0)) = :rating)
           AND (:priceRange IS NULL OR r.price_range = :priceRange)
        """, nativeQuery = true)
    List<Object[]> findRestaurantsByRatingAndPriceRange(@Param("rating") Integer rating,
                                                        @Param("priceRange") Integer priceRange);

    /* =====================
       NATIVE CREATE / READ
       ===================== */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO restaurants
            (user_id, address_id, name, price_range, phone, email, created_on, updated_on)
        VALUES
            (:userId, :addressId, :name, :priceRange, :phone, :email, NOW(), NOW())
        """, nativeQuery = true)
    int saveRestaurant(@Param("userId") int userId,
                       @Param("addressId") int addressId,
                       @Param("name") String name,
                       @Param("priceRange") int priceRange,
                       @Param("phone") String phone,
                       @Param("email") String email);

    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    int getLastInsertedId();

    @Query(value = """
        SELECT *
          FROM restaurants
         WHERE id = :id
        """, nativeQuery = true)
    Optional<Restaurant> findRestaurantById(@Param("id") int id);

    /* ======================
       NATIVE UPDATE / DELETE
       ====================== */
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE restaurants
           SET name = :name,
               price_range = :priceRange,
               phone = :phone,
               updated_on = NOW()
         WHERE id = :id
        """, nativeQuery = true)
    int updateRestaurant(@Param("id") int id,
                         @Param("name") String name,
                         @Param("priceRange") int priceRange,
                         @Param("phone") String phone);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM restaurants
         WHERE id = :id
        """, nativeQuery = true)
    int deleteRestaurantById(@Param("id") int id);

    /* ===========================
       LIST (for /api/restaurants)
       =========================== */
    @Query(value = "SELECT * FROM restaurants ORDER BY id DESC", nativeQuery = true)
    List<Restaurant> findAllOrderByIdDesc();
}
