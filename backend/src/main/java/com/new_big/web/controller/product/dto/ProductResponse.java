package com.new_big.web.controller.product.dto;

import com.new_big.web.enums.ProductType;
import com.new_big.web.enums.UnitType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

public record ProductResponse(
        int minimumStock,
        Boolean active,
        String name,
        String description,
        String barcode,
        String category,
        BigDecimal salePrice,
        BigDecimal costPrice,

        @Enumerated(EnumType.STRING)
        UnitType unitType,

        @Enumerated(EnumType.STRING)
        ProductType productType
) {
}
