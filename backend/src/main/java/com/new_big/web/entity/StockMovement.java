package com.new_big.web.entity;

import com.new_big.web.enums.MovementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue
    private Long id;

    private String observation;

    private Integer quantity;

    private LocalDateTime movementDate;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn( name = "batch_id", nullable = false)
    private Batch batch;

    @ManyToOne
    @JoinColumn(name = "saleItem_id")
    private SaleItem saleItem;
}
