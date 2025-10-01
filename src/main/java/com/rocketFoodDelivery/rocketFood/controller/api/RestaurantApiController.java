package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin
public class RestaurantApiController {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public RestaurantApiController(RestaurantRepository restaurantRepository,
                                   UserRepository userRepository,
                                   AddressRepository addressRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    // GET /api/restaurants
    @GetMapping
    public ResponseEntity<List<Restaurant>> list() {
        List<Restaurant> all = restaurantRepository.findAllOrderByIdDesc();
        return ResponseEntity.ok(all);
    }

    // GET /api/restaurants/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable int id) {
        return restaurantRepository.findRestaurantById(id)
                .or(() -> restaurantRepository.findById(id))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/restaurants
    @PostMapping
    public ResponseEntity<?> create(@RequestBody /*@jakarta.validation.Valid*/ ApiCreateRestaurantDto dto) {
        int userId = resolveUserId(dto);
        int addressId = resolveOrCreateAddressId(dto);

        if (userId == 0)    return ResponseEntity.badRequest().body(err("User not found"));
        if (addressId == 0) return ResponseEntity.badRequest().body(err("Address not found or invalid"));

        UserEntity owner = userRepository.findById(userId).orElse(null);
        Address addr = addressRepository.findById(addressId).orElse(null);
        if (owner == null)  return ResponseEntity.badRequest().body(err("User not found"));
        if (addr == null)   return ResponseEntity.badRequest().body(err("Address not found"));

        int inserted = restaurantRepository.saveRestaurant(
                owner.getId(),
                addr.getId(),
                dto.getName(),
                dto.getPriceRange(),
                dto.getPhone(),
                dto.getEmail()
        );
        if (inserted <= 0) return ResponseEntity.internalServerError().body(err("Insert failed"));

        int newId = restaurantRepository.getLastInsertedId();
        return restaurantRepository.findRestaurantById(newId)
                .<ResponseEntity<?>>map(saved ->
                        ResponseEntity.created(URI.create("/api/restaurants/" + saved.getId())).body(saved))
                .orElseGet(() -> ResponseEntity.status(500).body(err("Created restaurant not found")));
    }

    // PUT /api/restaurants/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody /*@jakarta.validation.Valid*/ ApiCreateRestaurantDto dto) {
        int updated = restaurantRepository.updateRestaurant(
                id,
                dto.getName(),
                dto.getPriceRange(),
                dto.getPhone()
        );
        if (updated <= 0) return ResponseEntity.notFound().build();

        return restaurantRepository.findRestaurantById(id)
                .or(() -> restaurantRepository.findById(id))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(500).body(err("Updated restaurant not found")));
    }

    // DELETE /api/restaurants/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        int deleted = restaurantRepository.deleteRestaurantById(id);
        if (deleted <= 0) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    // ---------- helpers (NO ApiUserDto needed) ----------

    private int resolveUserId(ApiCreateRestaurantDto dto) {
        // Only use flat ID — this avoids the missing ApiUserDto type completely
        return (dto.getUserId() > 0) ? dto.getUserId() : 0;
    }

    private int resolveOrCreateAddressId(ApiCreateRestaurantDto dto) {
        if (dto.getAddressId() > 0) return dto.getAddressId();
        if (dto.getAddress() != null && dto.getAddress().getId() > 0) return dto.getAddress().getId();

        if (dto.getAddress() != null) {
            Address a = new Address();
            a.setStreetAddress(dto.getAddress().getStreetAddress());
            a.setCity(dto.getAddress().getCity());
            a.setPostalCode(dto.getAddress().getPostalCode());
            return addressRepository.save(a).getId();
        }
        return 0;
    }

    private static ErrorMsg err(String m) { return new ErrorMsg(m); }
    private record ErrorMsg(String error) { }
}
