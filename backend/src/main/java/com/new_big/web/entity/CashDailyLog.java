package com.new_big.web.entity;

import com.new_big.web.enums.CashStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CashDailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // valor de abertura
    @Column(name = "opening_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal openingAmount;

    // valor informado pelo funcionário
    @Column(name = "closing_amount", precision = 12, scale = 2)
    private BigDecimal closingAmount;

    // valor de dinheiro físico experado no caixa
    @Column(name = "expected_closing_amount", precision = 12, scale = 2)
    private BigDecimal expectedClosingAmount;

    // total de venda de todos os metódos de pagamento
    @Column( nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    private CashStatus status;

    @ManyToOne
    @JoinColumn( name = "opened_by_id", nullable = false)
    private Employee openedBy;

    @ManyToOne
    @JoinColumn( name = "closed_by_id")
    private Employee closedBy;

    @OneToMany(mappedBy = "cashier", fetch = FetchType.LAZY)
    private List<Sale> sales =new ArrayList<>();

}
