package com.new_big.web.controller.product.dto;

import com.new_big.web.enums.ProductType;
import com.new_big.web.enums.UnitType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Nome é obrigatório")
    private String description;

    @NotBlank(message = "Categoria é obrigatório")
    private String category;

    @NotBlank(message = "Barcode é obrigatório")
    @Size(max = 3, message = "Barcode deve ter no máximo 3 caracteres")
    private String barcode;

    @NotNull
    @Positive
    private int minimumStock;

    @NotNull(message = "Um produto deve ser ativo ou inativo")
    private Boolean active;

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
