package com.rocketFoodDelivery.rocketFood.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.models.*;
import com.rocketFoodDelivery.rocketFood.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // no controller arg
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest // <-- don't pass OrderApiController.class (avoids "cannot be resolved" & package conflicts)
class OrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private OrderRepository orderRepository;
    @MockBean private RestaurantRepository restaurantRepository;
    @MockBean private CustomerRepository customerRepository;
    @MockBean private ProductRepository productRepository;
    @MockBean private ProductOrderRepository productOrderRepository;
    @MockBean private OrderStatusRepository orderStatusRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- GET SUCCESS: type=restaurants&id=7 ----------
    @Test
    @DisplayName("GET /api/orders?type=restaurants&id=7 -> 200 with filtered orders")
    void getOrdersByRestaurant_success() throws Exception {
        int restaurantId = 7;

        OrderStatus pending = new OrderStatus();
        pending.setName("pending");

        Restaurant r = new Restaurant();
        r.setId(restaurantId);

        Customer c1 = new Customer(); c1.setId(55);
        Customer c2 = new Customer(); c2.setId(56);

        Order o1 = new Order();
        o1.setId(101);
        o1.setRestaurant(r);
        o1.setCustomer(c1);
        o1.setOrder_status(pending);

        Order o2 = new Order();
        o2.setId(102);
        o2.setRestaurant(r);
        o2.setCustomer(c2);
        o2.setOrder_status(pending);

        Mockito.when(orderRepository.findByRestaurantId(restaurantId))
               .thenReturn(List.of(o1, o2));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/orders")
                        .param("type", "restaurants")
                        .param("id", String.valueOf(restaurantId))
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        // Controller returns a bare array (no envelope)
        .andExpect(jsonPath("$[0].id").value(101))
        .andExpect(jsonPath("$[0].restaurantId").value(restaurantId))
        .andExpect(jsonPath("$[0].customerId").value(55))
        .andExpect(jsonPath("$[0].status").value("pending"))
        .andExpect(jsonPath("$[1].id").value(102))
        .andExpect(jsonPath("$[1].restaurantId").value(restaurantId))
        .andExpect(jsonPath("$[1].customerId").value(56))
        .andExpect(jsonPath("$[1].status").value("pending"));
    }

    // ---------- GET FAILURE: simulate repository error on fallback -> 500 ----------
    @Test
    @DisplayName("GET /api/orders with repo error -> 500 (failure case)")
    void getOrders_internalError_failure() throws Exception {
        // Your controller falls back to orderRepository.findAll() when type is unknown.
        Mockito.when(orderRepository.findAll())
               .thenThrow(new RuntimeException("DB is down"));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/orders")
                        .param("type", "unknown")
                        .param("id", "7")
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().is5xxServerError());
    }

    // ---------- POST SUCCESS ----------
    @Test
    @DisplayName("POST /api/orders with valid payload -> 201 Created and returns OrderResponse")
    void createOrder_success() throws Exception {
        // Matches your CreateOrderRequest fields
        String payload = """
        {
          "restaurantId": 7,
          "customerId": 55,
          "items": [
            {"productId": 1, "quantity": 2},
            {"productId": 3, "quantity": 1}
          ]
        }
        """;

        Restaurant r = new Restaurant(); r.setId(7);
        Customer c = new Customer(); c.setId(55);

        Mockito.when(restaurantRepository.findById(7)).thenReturn(Optional.of(r));
        Mockito.when(customerRepository.findById(55)).thenReturn(Optional.of(c));

        OrderStatus pending = new OrderStatus(); pending.setName("pending");
        Mockito.when(orderStatusRepository.findByNameNative("pending")).thenReturn(pending);

        Order saved = new Order();
        saved.setId(999);
        saved.setRestaurant(r);
        saved.setCustomer(c);
        saved.setOrder_status(pending);

        Mockito.when(orderRepository.save(any(Order.class))).thenReturn(saved);

        Product p1 = new Product(); p1.setId(1);
        Product p3 = new Product(); p3.setId(3);
        Mockito.when(productRepository.findById(1)).thenReturn(Optional.of(p1));
        Mockito.when(productRepository.findById(3)).thenReturn(Optional.of(p3));

        Mockito.when(productOrderRepository.saveAll(anyList()))
               .thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isCreated())
        // Simpler header check (no ambiguous endsWith)
        .andExpect(header().string("Location",
                org.hamcrest.Matchers.containsString("/api/orders/999")))
        .andExpect(jsonPath("$.id").value(999))
        .andExpect(jsonPath("$.restaurantId").value(7))
        .andExpect(jsonPath("$.customerId").value(55))
        .andExpect(jsonPath("$.status").value("pending"))
        .andExpect(jsonPath("$.items[0].productId").value(1))
        .andExpect(jsonPath("$.items[0].quantity").value(2))
        .andExpect(jsonPath("$.items[1].productId").value(3))
        .andExpect(jsonPath("$.items[1].quantity").value(1));
    }

    // ---------- POST FAILURE (invalid payload triggers your 400) ----------
    @Test
    @DisplayName("POST /api/orders with empty items -> 400 Bad Request with error")
    void createOrder_invalidPayload_failure() throws Exception {
        String badPayload = """
        {
          "restaurantId": 7,
          "customerId": 55,
          "items": []
        }
        """;

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badPayload)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error",
                org.hamcrest.Matchers.containsStringIgnoringCase("invalid order payload")));
    }
}
