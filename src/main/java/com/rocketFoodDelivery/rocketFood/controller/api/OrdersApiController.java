// src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/OrdersApiController.java
package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.models.*;
import com.rocketFoodDelivery.rocketFood.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class OrdersApiController {

    private final OrderRepository orderRepository;
    private final ProductOrderRepository productOrderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderStatusRepository orderStatusRepository;

    public OrdersApiController(OrderRepository orderRepository,
                            ProductOrderRepository productOrderRepository,
                            ProductRepository productRepository,
                            CustomerRepository customerRepository,
                            RestaurantRepository restaurantRepository,
                            OrderStatusRepository orderStatusRepository) {
        this.orderRepository = orderRepository;
        this.productOrderRepository = productOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderStatusRepository = orderStatusRepository;
    }

    // GET /api/orders  (supports ?type=restaurants&id={restaurantId})
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> listOrders(@RequestParam(value = "type", required = false) String type,
                                                          @RequestParam(value = "id", required = false) Integer id) {
        List<Order> orders;
        if ("restaurants".equalsIgnoreCase(type) && id != null) {
            orders = orderRepository.findByRestaurantId(id);
        } else {
            orders = orderRepository.findAll().stream()
                    .sorted(Comparator.comparingInt(Order::getId).reversed())
                    .collect(Collectors.toList());
        }
        List<OrderResponse> resp = orders.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(resp);
    }

    // POST /api/orders
    @PostMapping("/orders")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest req) {
        if (req == null || req.restaurantId == null || req.customerId == null || req.items == null || req.items.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid order payload"));
        }

        Optional<Restaurant> restaurant = restaurantRepository.findById(req.restaurantId);
        Optional<Customer> customer = customerRepository.findById(req.customerId);
        if (restaurant.isEmpty() || customer.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Restaurant or customer not found"));
        }

        orderRepository.ensureStatusExists("pending");
        OrderStatus pending = orderStatusRepository.findByNameNative("pending");

        Order order = new Order();
        order.setRestaurant(restaurant.get());
        order.setCustomer(customer.get());
        if (pending != null) order.setOrder_status(pending);

        Order saved = orderRepository.save(order);

        List<ProductOrder> lines = new ArrayList<>();
        for (CreateOrderItem item : req.items) {
            if (item == null || item.productId == null || item.quantity == null || item.quantity <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Each item must have productId and positive quantity"));
            }
            Product prod = productRepository.findById(item.productId).orElse(null);
            if (prod == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Product not found: " + item.productId));
            }
            ProductOrder po = new ProductOrder();
            po.setOrder(saved);
            po.setProduct(prod);
            po.setProduct_quantity(item.quantity);
            lines.add(po);
        }
        productOrderRepository.saveAll(lines);

        OrderResponse resp = toResponse(saved);
        resp.items = lines.stream()
                .map(po -> new OrderItemResponse(po.getProduct().getId(), po.getProduct_quantity()))
                .toList();

        return ResponseEntity.created(URI.create("/api/orders/" + saved.getId())).body(resp);
    }

    private OrderResponse toResponse(Order o) {
        String statusName = (o.getOrder_status() != null) ? o.getOrder_status().getName() : null;
        return new OrderResponse(
                o.getId(),
                (o.getCustomer() != null ? o.getCustomer().getId() : null),
                (o.getRestaurant() != null ? o.getRestaurant().getId() : null),
                statusName,
                List.of()
        );
    }

    // ---------- DTOs ----------

    public static class CreateOrderRequest {
        public Integer customerId;
        public Integer restaurantId;
        public List<CreateOrderItem> items;
    }

    public static class CreateOrderItem {
        public Integer productId;
        public Integer quantity;
    }

    public static class OrderResponse {
        public Integer id;
        public Integer customerId;
        public Integer restaurantId;
        public String status;
        public List<OrderItemResponse> items;

        public OrderResponse(Integer id, Integer customerId, Integer restaurantId, String status,
                             List<OrderItemResponse> items) {
            this.id = id;
            this.customerId = customerId;
            this.restaurantId = restaurantId;
            this.status = status;
            this.items = items;
        }
    }

    public static class OrderItemResponse {
        public Integer productId;
        public Integer quantity;

        public OrderItemResponse(Integer productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
