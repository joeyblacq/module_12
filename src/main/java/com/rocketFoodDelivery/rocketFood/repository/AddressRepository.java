package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Address;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    // Optional helper: newest first
    List<Address> findAllByOrderByIdDesc();

    // POST /api/address (native)
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO addresses (street, city, postal_code, created_on, updated_on)
        VALUES (:street, :city, :postalCode, NOW(), NOW())
        """, nativeQuery = true)
    int insertAddress(@Param("street") String street,
                      @Param("city") String city,
                      @Param("postalCode") String postalCode);

    // Obtain id of last inserted row when needed
    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    Integer lastInsertId();
}
