package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findById(int id);
    List<Order> findByCustomerId(int id);
    List<Order> findByRestaurantId(int id);
    List<Order> findByCourierId(int id);

    // Return all orders for a restaurant (maps to Order entity)
@Query(value = "SELECT * FROM orders WHERE restaurant_id = :restaurantId ORDER BY id DESC", nativeQuery = true)
List<Order> findOrdersByRestaurantId(@Param("restaurantId") int restaurantId);

// Hard delete an order by id
@Modifying
@Transactional
@Query(value = "DELETE FROM orders WHERE id = :orderId", nativeQuery = true)
void deleteOrderById(@Param("orderId") int orderId);
}
