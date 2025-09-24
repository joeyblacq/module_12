package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;
import com.rocketFoodDelivery.rocketFood.exception.*;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
public class RestaurantApiController {
    private final RestaurantService restaurantService;

    @Autowired
    public RestaurantApiController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * Creates a new restaurant.
     * Returns 201 Created with Location header on success.
     */
    @PostMapping("/api/restaurants")
    public ResponseEntity<Object> createRestaurant(@Valid @RequestBody ApiCreateRestaurantDto restaurant) {
        Optional<ApiCreateRestaurantDto> created = restaurantService.createRestaurant(restaurant);
        if (created.isEmpty()) {
            throw new BadRequestException("Invalid restaurant payload or user/address missing");
        }
        URI location = URI.create("/api/restaurants/" + created.get().getId());
        return ResponseEntity.created(location).body(created.get());
    }

    /**
     * Deletes a restaurant by ID.
     * Returns 204 No Content on success.
     */
    @DeleteMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> deleteRestaurant(@PathVariable int id){
        // Guard: 404 if it does not exist
        if (restaurantService.findById(id).isEmpty()) {
            throw new ResourceNotFoundException(String.format("Restaurant with id %d not found", id));
        }
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates an existing restaurant by ID.
     * Uses name/priceRange/phone (per service behavior).
     * Returns 200 OK with updated payload.
     */
    @PutMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> updateRestaurant(
            @PathVariable("id") int id,
            @Valid @RequestBody ApiCreateRestaurantDto restaurantUpdateData,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            throw new BadRequestException("Validation failed for update payload");
        }

        Optional<ApiCreateRestaurantDto> updated = restaurantService.updateRestaurant(id, restaurantUpdateData);
        if (updated.isEmpty()) {
            throw new ResourceNotFoundException(String.format("Restaurant with id %d not found", id));
        }
        return ResponseBuilder.buildOkResponse(updated.get());
    }

    /**
     * Retrieves restaurant details with average rating.
     */
    @GetMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> getRestaurantById(@PathVariable int id) {
        Optional<ApiRestaurantDto> restaurantWithRatingOptional = restaurantService.findRestaurantWithAverageRatingById(id);
        if (!restaurantWithRatingOptional.isPresent())
            throw new ResourceNotFoundException(String.format("Restaurant with id %d not found", id));
        return ResponseBuilder.buildOkResponse(restaurantWithRatingOptional.get());
    }

    /**
     * Lists restaurants filtered by optional rating and price_range.
     */
    @GetMapping("/api/restaurants")
    public ResponseEntity<Object> getAllRestaurants(
        @RequestParam(name = "rating", required = false) Integer rating,
        @RequestParam(name = "price_range", required = false) Integer priceRange
    ) {
        return ResponseBuilder.buildOkResponse(
            restaurantService.findRestaurantsByRatingAndPriceRange(rating, priceRange)
        );
    }
}
