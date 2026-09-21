package com.new_big.web.entity;

import com.new_big.web.controller.product.dto.ProductRequestDTO;
import com.new_big.web.enums.ProductType;
import com.new_big.web.enums.UnitType;
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
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    private int minimumStock;

    private Boolean active;

    private String name;

    private String description;

    private String barcode;

    private String category;

    private BigDecimal salePrice;

    private BigDecimal costPrice;

    @Enumerated(EnumType.STRING)
    private UnitType unitType;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    public Product(ProductRequestDTO data){
        this.name = data.name();
        this.description = data.description();
        this.barcode = data.barcode();
        this.category = data.category();
        this.salePrice = data.salePrice();
        this.costPrice = data.costPrice();
        this.active = data.active() == null || data.active();
    }
}
