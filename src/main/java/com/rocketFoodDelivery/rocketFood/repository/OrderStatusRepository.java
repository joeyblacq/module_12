package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Integer> {

    /* ==========================
       FIND BY NAME (NATIVE)
       ========================== */
    @Query(value = """
        SELECT *
          FROM order_status
         WHERE name = :name
         LIMIT 1
        """, nativeQuery = true)
    OrderStatus findByNameNative(@Param("name") String name);
}
