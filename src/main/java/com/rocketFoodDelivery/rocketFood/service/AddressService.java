package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {
    private final AddressRepository repo;

    public AddressService(AddressRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Address create(ApiAddressDto dto) {
        repo.saveAddress(dto.getStreetAddress(), dto.getCity(), dto.getPostalCode());
        int id = repo.getLastInsertedId();
        return repo.findById(id).orElseThrow();
    }
}
