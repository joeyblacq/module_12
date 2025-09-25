package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {
    private final AddressRepository repo;
    public AddressService(AddressRepository repo) { this.repo = repo; }

    @Transactional
    public Address create(ApiAddressDto dto) {
        Address a = new Address();
        a.setStreetAddress(dto.getStreetAddress());
        a.setCity(dto.getCity());
        a.setPostalCode(dto.getPostalCode());
        return repo.save(a);
    }
}
