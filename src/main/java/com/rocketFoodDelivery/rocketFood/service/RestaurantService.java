package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductOrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductOrderRepository productOrderRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;

    @Autowired
    public RestaurantService(
            RestaurantRepository restaurantRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            ProductOrderRepository productOrderRepository,
            UserRepository userRepository,
            AddressService addressService
    ) {
        this.restaurantRepository = restaurantRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.productOrderRepository = productOrderRepository;
        this.userRepository = userRepository;
        this.addressService = addressService;
    }

    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Optional<ApiRestaurantDto> findRestaurantWithAverageRatingById(int id) {
        List<Object[]> restaurant = restaurantRepository.findRestaurantWithAverageRatingById(id);
        if (restaurant.isEmpty()) return Optional.empty();

        Object[] row = restaurant.get(0);
        int restaurantId = (int) row[0];
        String name = (String) row[1];
        int priceRange = (int) row[2];
        double rating = (row[3] != null) ? ((BigDecimal) row[3]).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
        int roundedRating = (int) Math.ceil(rating);
        return Optional.of(new ApiRestaurantDto(restaurantId, name, priceRange, roundedRating));
    }

    public List<ApiRestaurantDto> findRestaurantsByRatingAndPriceRange(Integer rating, Integer priceRange) {
        List<Object[]> rows = restaurantRepository.findRestaurantsByRatingAndPriceRange(rating, priceRange);
        List<ApiRestaurantDto> out = new ArrayList<>();
        for (Object[] row : rows) {
            int restaurantId = (int) row[0];
            String name = (String) row[1];
            int range = (int) row[2];
            double avgRating = (row[3] != null) ? ((BigDecimal) row[3]).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
            int roundedAvgRating = (int) Math.ceil(avgRating);
            out.add(new ApiRestaurantDto(restaurantId, name, range, roundedAvgRating));
        }
        return out;
    }

    // ------------------ CREATE ------------------

    /**
     * Creates a new restaurant using native INSERT for restaurants.
     * - Creates the nested address first via AddressService.create(ApiAddressDto)
     * - Inserts restaurant using repository.saveRestaurant(...)
     * - Returns created data (id + echoed fields)
     */
    @Transactional
    public Optional<ApiCreateRestaurantDto> createRestaurant(ApiCreateRestaurantDto req) {
        if (req == null) return Optional.empty();
        if (userRepository.findById(req.getUserId()).isEmpty()) return Optional.empty();
        ApiAddressDto addrDto = req.getAddress();
        if (addrDto == null) return Optional.empty();

        // 1) create address (matches AddressService.create(ApiAddressDto) signature)
        Address savedAddr = addressService.create(addrDto);

        // 2) insert restaurant (ints widen to long automatically; no .longValue() calls)
        restaurantRepository.saveRestaurant(
                req.getUserId(),
                savedAddr.getId(),
                req.getName(),
                req.getPriceRange(),
                req.getPhone(),
                req.getEmail()
        );

        int newId = restaurantRepository.getLastInsertedId();
        Restaurant saved = restaurantRepository.findRestaurantById(newId).orElse(null);
        if (saved == null) return Optional.empty();

        // 3) build response; avoid Address.getStreet() by using the DTO values + generated id
        ApiCreateRestaurantDto out = new ApiCreateRestaurantDto();
        out.setId(saved.getId());
        out.setUserId(req.getUserId());
        out.setName(saved.getName());
        out.setPriceRange(saved.getPriceRange());
        out.setPhone(saved.getPhone());
        out.setEmail(saved.getEmail());
        out.setAddress(new ApiAddressDto(
                savedAddr.getId(),
                addrDto.getStreetAddress(),
                addrDto.getCity(),
                addrDto.getPostalCode()
        ));
        return Optional.of(out);
    }

    // ------------------ READ ------------------

    public Optional<Restaurant> findById(int id) {
        return restaurantRepository.findRestaurantById(id);
    }

    // ------------------ UPDATE ------------------

    /**
     * Updates name/price_range/phone via native UPDATE; returns updated data.
     * (Address updates are not handled here.)
     */
    @Transactional
    public Optional<ApiCreateRestaurantDto> updateRestaurant(int id, ApiCreateRestaurantDto update) {
        Optional<Restaurant> before = restaurantRepository.findRestaurantById(id);
        if (before.isEmpty() || update == null) return Optional.empty();

        restaurantRepository.updateRestaurant(id, update.getName(), update.getPriceRange(), update.getPhone());
        Restaurant after = restaurantRepository.findRestaurantById(id).orElse(null);
        if (after == null) return Optional.empty();

        ApiCreateRestaurantDto out = new ApiCreateRestaurantDto();
        out.setId(after.getId());
        out.setUserId(before.get().getUserEntity() != null ? before.get().getUserEntity().getId() : update.getUserId());
        out.setName(after.getName());
        out.setPriceRange(after.getPriceRange());
        out.setPhone(after.getPhone());
        out.setEmail(after.getEmail());
        // keep whatever caller sent for address (we didn't change it here)
        out.setAddress(update.getAddress());
        return Optional.of(out);
    }

    // ------------------ DELETE ------------------

    /**
     * Deletes a restaurant and related rows (product_orders -> orders -> products -> restaurant).
     */
    @Transactional
    public void deleteRestaurant(int restaurantId) {
        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);
        for (Order o : orders) productOrderRepository.deleteProductOrdersByOrderId(o.getId());
        for (Order o : orders) orderRepository.deleteOrderById(o.getId());
        productRepository.deleteProductsByRestaurantId(restaurantId);
        restaurantRepository.deleteRestaurantById(restaurantId);
    }
}
