// ===================== OrderStatusController.java =====================
package com.rocketFoodDelivery.rocketFood.controller;

import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderStatusController {
    private final OrderRepository orderRepository;

    public OrderStatusController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // POST /api/order/{order_id}/status  (body: {"status_id": <int>})
    @PostMapping("/api/order/{order_id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable("order_id") int orderId,
                                             @RequestBody StatusUpdate body) {
        int rows = orderRepository.updateOrderStatus(orderId, body.status_id());
        if (rows == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    public record StatusUpdate(@NotNull @Min(1) Integer status_id) {}
}

