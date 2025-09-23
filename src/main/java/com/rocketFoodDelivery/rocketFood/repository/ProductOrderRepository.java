package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {

    @Modifying
@Transactional
@Query(value = "DELETE FROM product_orders WHERE order_id = :orderId", nativeQuery = true)
void deleteProductOrdersByOrderId(@Param("orderId") int orderId);
}
