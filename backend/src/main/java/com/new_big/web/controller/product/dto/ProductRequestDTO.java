package com.new_big.web.controller.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank
        String name,

        @NotNull
        BigDecimal salePrice
) {
}