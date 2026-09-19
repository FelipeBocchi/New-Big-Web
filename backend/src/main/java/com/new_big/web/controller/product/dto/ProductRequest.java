package com.new_big.web.controller.product.dto;

import com.new_big.web.enums.ProductType;
import com.new_big.web.enums.UnitType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotNull
    @Positive
    private int minimumStock;

    @NotNull(message = "Um cliente deve ser ativo ou inativo")
    private Boolean active;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Nome é obrigatório")
    private String description;

    @NotBlank(message = "Nome é obrigatório")
    private String barcode;

    @NotBlank(message = "Nome é obrigatório")
    private String category;

    @NotNull
    @Positive
    private BigDecimal salePrice;

    @NotNull
    @Positive
    private BigDecimal costPrice;

    @Enumerated(EnumType.STRING)
    private UnitType unitType;

    @Enumerated(EnumType.STRING)
    private ProductType productType;
}
