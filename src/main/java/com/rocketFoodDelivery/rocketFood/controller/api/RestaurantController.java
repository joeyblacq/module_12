package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * GET /api/restaurants
     * Optional filters:
     * - rating (Integer)
     * - price_range (Integer)
     * Always returns a list of ApiRestaurantDto with computed rating.
     */
    @GetMapping("/restaurants")
    public ResponseEntity<List<ApiRestaurantDto>> getRestaurants(
            @RequestParam(name = "rating", required = false) Integer rating,
            @RequestParam(name = "price_range", required = false) Integer priceRange
    ) {
        List<ApiRestaurantDto> out = restaurantService.findRestaurantsByRatingAndPriceRange(rating, priceRange);
        return ResponseEntity.ok(out);
    }

    /**
     * GET /api/restaurants/{id}
     * Returns ApiRestaurantDto with average rating (rounded up) for the given restaurant id.
     * 404 if not found.
     */
    @GetMapping("/restaurants/{id}")
    public ResponseEntity<ApiRestaurantDto> getRestaurantById(@PathVariable("id") int id) {
        Optional<ApiRestaurantDto> maybe = restaurantService.findRestaurantWithAverageRatingById(id);
        return maybe.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
    }

    /**
     * POST /api/restaurants
     * Body: ApiCreateRestaurantDto (expects userId + address + basic fields).
     * Creates address first, then inserts restaurant via native SQL.
     * Returns 201 with Location header and created payload.
     */
    @PostMapping("/restaurants")
    public ResponseEntity<ApiCreateRestaurantDto> createRestaurant(
            @Valid @RequestBody ApiCreateRestaurantDto req
    ) {
        Optional<ApiCreateRestaurantDto> maybe = restaurantService.createRestaurant(req);
        if (maybe.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid restaurant payload or user/address missing");
        }
        ApiCreateRestaurantDto created = maybe.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/api/restaurants/" + created.getId()));
        return new ResponseEntity<>(created, headers, HttpStatus.CREATED);
    }

    /**
     * PUT /api/restaurants/{id}
     * Body: ApiCreateRestaurantDto (name/priceRange/phone used by native update; email unchanged here)
     * Returns updated view payload or 404.
     */
    @PutMapping("/restaurants/{id}")
    public ResponseEntity<ApiCreateRestaurantDto> updateRestaurant(
            @PathVariable("id") int id,
            @Valid @RequestBody ApiCreateRestaurantDto update
    ) {
        Optional<ApiCreateRestaurantDto> maybe = restaurantService.updateRestaurant(id, update);
        return maybe.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
    }

    /**
     * DELETE /api/restaurants/{id}
     * Deletes in correct order: product_orders -> orders -> products -> restaurant.
     * Returns 204 always (idempotent).
     */
    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable("id") int id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}