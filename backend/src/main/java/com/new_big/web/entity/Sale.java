package com.new_big.web.entity;

import com.new_big.web.enums.PaymentMethod;
import com.new_big.web.enums.SaleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 12, scale = 2)
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

    @OneToMany(mappedBy = "sale", fetch = FetchType.LAZY)
    private List<SaleItem> saleItems;

    @OneToMany(mappedBy = "sale", fetch = FetchType.LAZY)
    private List<StockMovement> stockMovements;

    // duas funções para poder ir adicionando item e movimentações a venda e adicionando venda neles
    public void addSaleItem(SaleItem item) {
        this.saleItems.add(item);
        item.setSale(this);
    }

    public void addStockMovement(StockMovement movement) {
        this.stockMovements.add(movement);
        movement.setSale(this);
    }

}
