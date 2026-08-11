package com.new_big.web.entity;

import com.new_big.web.enums.PaymentMethod;
import com.new_big.web.enums.SaleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue
    private Long id;

    private BigDecimal subtotal;

    private BigDecimal discount;

    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private SaleStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn( name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn( name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn( name = "cash_daily_log_id", nullable = false)
    private CashDailyLog cashier;

}
