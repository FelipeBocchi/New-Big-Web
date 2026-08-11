package com.new_big.web.entity;

import com.new_big.web.enums.CartStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SelfServiceCart {

    @Id
    @GeneratedValue
    private Long id;

    private String token;

    @Column(unique = true, nullable = false)
    private BigDecimal total;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private CartStatus status;

    @ManyToOne
    @JoinColumn( name = "customer_id")
    private Customer customer;
}
