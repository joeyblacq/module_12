// src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/OrderStatusApiController.java
package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.OrderStatusRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class OrderStatusApiController {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    public OrderStatusApiController(OrderRepository orderRepository,
                                 OrderStatusRepository orderStatusRepository) {
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
    }

    @PostMapping("/order/{orderId}/status")
    @Transactional
    public ResponseEntity<?> setStatus(@PathVariable("orderId") int orderId,
                                       @RequestBody Map<String, String> body) {
        String statusName = body.getOrDefault("status", "").trim();
        if (statusName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'status' in body"));
        }

        Optional<Order> maybeOrder = orderRepository.findById(orderId);
        if (maybeOrder.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Order with id " + orderId + " not found"));
        }

        orderRepository.ensureStatusExists(statusName);
        int updated = orderRepository.updateOrderStatus(orderId, statusName);
        if (updated == 0) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update order status"));
        }

        OrderStatus status = orderStatusRepository.findByNameNative(statusName);
        return ResponseEntity.ok(Map.of(
                "message", "Status updated",
                "orderId", orderId,
                "status", status != null ? status.getName() : statusName
        ));
    }
}
