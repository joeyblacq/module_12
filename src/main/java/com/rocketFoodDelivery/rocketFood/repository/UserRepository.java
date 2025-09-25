  package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository <UserEntity, Integer> {

    @Query(
        nativeQuery = true,
        value = """
            SELECT *
            FROM users
            WHERE id = :id AND active = TRUE
        """
    )
    List<UserEntity> findAllByOrderByIdDesc();
    Optional<UserEntity> findByEmail(String email);


}
