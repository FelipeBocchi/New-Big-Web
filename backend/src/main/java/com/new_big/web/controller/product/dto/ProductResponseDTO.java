package com.new_big.web.controller.product.dto;

import com.new_big.web.entity.Product;

import java.math.BigDecimal;

public record ProductResponseDTO(
    Long id,
    String name,
    String description,
    String barCode,
    String category,
    BigDecimal salePrice,
    BigDecimal costPrice,
    String status
) {
    public ProductResponseDTO(Product product){
    this(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getBarcode(),
        product.getCategory(),
        product.getSalePrice(),
        product.getCostPrice(),
        Boolean.FALSE.equals(product.getActive()) ? "Inativo" : "Ativo"
    );
    }
}