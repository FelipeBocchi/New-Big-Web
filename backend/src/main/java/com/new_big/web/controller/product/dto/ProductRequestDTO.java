package com.new_big.web.controller.product.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank
        String name,

        String description,

        @JsonAlias("barCode")
        String barcode,

        String category,

        @NotNull
        BigDecimal salePrice,

        BigDecimal costPrice,

        Boolean active
) {
}