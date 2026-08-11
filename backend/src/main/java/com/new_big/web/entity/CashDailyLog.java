package com.new_big.web.entity;

import com.new_big.web.enums.CashStatus;
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
public class CashDailyLog {

    @Id
    @GeneratedValue
    private Long id;

    private BigDecimal openingAmount;

    private BigDecimal closingAmount;

    private BigDecimal total;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    private CashStatus status;

    @ManyToOne
    @JoinColumn( name = "opened_by_id", nullable = false)
    private Employee openedBy;

    @ManyToOne
    @JoinColumn( name = "closed_by_id")
    private Employee closedBy;

}
