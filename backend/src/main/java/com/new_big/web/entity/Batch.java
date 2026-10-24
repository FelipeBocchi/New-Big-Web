package com.new_big.web.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String batchCode;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Boolean expiration;

    @Column(name = "quantity_is_zero", nullable = false)
    private Boolean quantityIsZero;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "expiration_at")
    private LocalDate expirationAt;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
