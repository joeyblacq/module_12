package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Order;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // GET /api/orders?type=restaurants&id={id} (native)
    @Query(value = """
        SELECT *
          FROM orders
         WHERE restaurant_id = :restaurantId
         ORDER BY id DESC
        """, nativeQuery = true)
    List<Order> findByRestaurantId(@Param("restaurantId") int restaurantId);

    // DELETE /api/order/{id} (native)
    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM orders
         WHERE id = :orderId
        """, nativeQuery = true)
    int deleteOrderById(@Param("orderId") int orderId);

    // (Keeps your status helpers if you use them elsewhere)
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO order_status (name)
        SELECT :statusName
         WHERE NOT EXISTS (
            SELECT 1 FROM order_status WHERE name = :statusName
         )
        """, nativeQuery = true)
    void ensureStatusExists(@Param("statusName") String statusName);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE orders o
        JOIN order_status s ON s.name = :statusName
           SET o.status_id = s.id
         WHERE o.id = :orderId
        """, nativeQuery = true)
    int updateOrderStatus(@Param("orderId") int orderId, @Param("statusName") String statusName);
}
