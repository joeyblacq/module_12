package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_orders",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "order_id"}))
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Min(1)
    @Column(nullable = false)
    private Integer product_quantity;

    @Min(0)
    private Integer product_unit_cost;

    @PrePersist
    private void validateBeforePersist() {
        if (!productBelongsToRestaurant()) {
            throw new IllegalArgumentException("ProductOrder instance is not valid");
        }
    }

    private boolean productBelongsToRestaurant() {
        return product.getRestaurant().equals(order.getRestaurant());
    }
}
