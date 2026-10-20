package com.new_big.web.controller.product.dto;

import com.new_big.web.entity.Product;
import com.new_big.web.enums.ProductType;
import com.new_big.web.enums.UnitType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

public record ProductResponse(
        String name,
        String description,
        String barcode,
        String category,
        int minimumStock,
        Boolean active,
        BigDecimal salePrice,
        BigDecimal costPrice,

        @Enumerated(EnumType.STRING)
        UnitType unitType,

        @Enumerated(EnumType.STRING)
        ProductType productType
) {
        public static ProductResponse de(Product product) {
                return new ProductResponse(
                        product.getName(),
                        product.getDescription(),
                        product.getBarcode(),
                        product.getCategory(),
                        product.getMinimumStock(),
                        product.getActive(),
                        product.getSalePrice(),
                        product.getCostPrice(),
                        product.getUnitType(),
                        product.getProductType()
                );
        }
}
