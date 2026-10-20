package com.new_big.web.controller.saleController.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SaleAddRequest {

    @NotNull(message = "id da venda é obrigatório")
    private Long id;

    @NotNull(message = "id do caixa aberto é obrigatório")
    private Long cashId;

    @NotBlank(message = "o código de barra do produto é obrigatório")
    private String barcode;

    private BigDecimal discountUnit;

    @Positive
    @NotNull(message = "deve have ruma quantidade desse produto")
    private int quantity;
}
