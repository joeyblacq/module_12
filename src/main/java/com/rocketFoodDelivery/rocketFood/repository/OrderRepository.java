package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Order;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query(value = """
        SELECT *
          FROM orders
         WHERE restaurant_id = :restaurantId
         ORDER BY id
        """, nativeQuery = true)
    List<Order> findByRestaurantId(@Param("restaurantId") int restaurantId);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM orders
         WHERE id = :orderId
        """, nativeQuery = true)
    int deleteOrderById(@Param("orderId") int orderId);

    // (Keep this if you already added it)
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE orders
           SET order_status_id = :statusId
         WHERE id = :orderId
        """, nativeQuery = true)
    int updateOrderStatus(@Param("orderId") int orderId,
                          @Param("statusId") int statusId);
}
