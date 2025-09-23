package com.rocketFoodDelivery.rocketFood.controller;

import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // DB column is 'street'
    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "city", nullable = false)
    private String city;

    // DB column is 'postal_code' (snake_case)
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    // ---- Constructors ----
    public Address() {}

    public Address(Integer id, String street, String city, String postalCode) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
    }

    // ---- Getters (these are what your code calls) ----
    public Integer getId() { return id; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }

    // ---- Setters ----
    public void setId(Integer id) { this.id = id; }
    public void setStreet(String street) { this.street = street; }
    public void setCity(String city) { this.city = city; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
