package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    // GET /api/user/{id} (native)
    @Query(value = """
        SELECT *
          FROM users
         WHERE id = :id
         LIMIT 1
        """, nativeQuery = true)
    Optional<UserEntity> findUserByIdNative(@Param("id") int id);

    // Used by auth (if needed)
    Optional<UserEntity> findByEmail(String email);
}
