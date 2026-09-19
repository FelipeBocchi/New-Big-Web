package com.new_big.web.controller.product.dto;

import com.new_big.web.entity.Product;

import java.math.BigDecimal;

public record ProductResponseDTO(Long id, String name, BigDecimal salePrice) {
    public ProductResponseDTO(Product product){
        this(product.getId(), product.getName(), product.getSalePrice());
    }
}