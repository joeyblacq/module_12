package com.rocketFoodDelivery.rocketFood.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RestaurantApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    // keep if your controller touches it via the service layer; harmless to mock
    @MockBean
    private UserRepository userRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    // -------------------- POST /api/restaurants --------------------

    @Test
    @DisplayName("POST /api/restaurants — Success returns 201 and data envelope")
    public void testCreateRestaurant_Success() throws Exception {
        ApiAddressDto inputAddress = new ApiAddressDto(1, "123 Wellington St.", "Montreal", "H1H2H2");
        ApiCreateRestaurantDto inputRestaurant =
                new ApiCreateRestaurantDto(1, 4, 1, "Villa wellington", 2, "5144154415",
                        "reservations@villawellington.com", inputAddress);

        // Service returns created DTO
        when(restaurantService.createRestaurant(any())).thenReturn(Optional.of(inputRestaurant));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(inputRestaurant))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.id").exists())
        .andExpect(jsonPath("$.data.name").value(inputRestaurant.getName()))
        .andExpect(jsonPath("$.data.phone").value(inputRestaurant.getPhone()))
        .andExpect(jsonPath("$.data.email").value(inputRestaurant.getEmail()))
        .andExpect(jsonPath("$.data.address.id").exists())
        .andExpect(jsonPath("$.data.address.city").value(inputRestaurant.getAddress().getCity()))
        .andExpect(jsonPath("$.data.address.street_address").value(inputRestaurant.getAddress().getStreetAddress()))
        .andExpect(jsonPath("$.data.address.postal_code").value(inputRestaurant.getAddress().getPostalCode()))
        .andExpect(jsonPath("$.data.user_id").value(inputRestaurant.getUserId()))
        .andExpect(jsonPath("$.data.price_range").value(inputRestaurant.getPriceRange()));
    }

    @Test
    @DisplayName("POST /api/restaurants — Failure returns 400 on invalid payload")
    public void testCreateRestaurant_Failure_InvalidPayload() throws Exception {
        // Build an invalid payload (missing required fields) and force service to fail
        ApiCreateRestaurantDto bad = new ApiCreateRestaurantDto(); // empty/invalid by design
        when(restaurantService.createRestaurant(any())).thenReturn(Optional.empty());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message",
                org.hamcrest.Matchers.containsStringIgnoringCase("invalid")));
    }

    // -------------------- PUT /api/restaurants/{id} --------------------

    @Test
    @DisplayName("PUT /api/restaurants/{id} — Success returns 200 and updated data")
    public void testUpdateRestaurant_Success() throws Exception {
        int restaurantId = 1;
        ApiCreateRestaurantDto updatedData = new ApiCreateRestaurantDto();
        updatedData.setName("Updated Name");
        updatedData.setPriceRange(2);
        updatedData.setPhone("555-1234");

        when(restaurantService.updateRestaurant(restaurantId, updatedData))
                .thenReturn(Optional.of(updatedData));

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/restaurants/{id}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedData))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.id").exists())
        .andExpect(jsonPath("$.data.name").value("Updated Name"))
        .andExpect(jsonPath("$.data.price_range").value(2))
        .andExpect(jsonPath("$.data.phone").value("555-1234"));
    }

    @Test
    @DisplayName("PUT /api/restaurants/{id} — Failure returns 404 when not found")
    public void testUpdateRestaurant_Failure_NotFound() throws Exception {
        int missingId = 9999;
        ApiCreateRestaurantDto body = new ApiCreateRestaurantDto();
        body.setName("Doesn't matter here");

        when(restaurantService.updateRestaurant(missingId, body)).thenReturn(Optional.empty());

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/restaurants/{id}", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body))
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message",
                org.hamcrest.Matchers.containsStringIgnoringCase("not found")));
    }
}
