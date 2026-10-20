package com.new_big.web.controller.cashDailyController.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CashDailyOpenedRequest {

    @Positive
    @NotNull(message = "Valor de aberturo de caixa é obrigatório")
    private BigDecimal openingAmount;

    @NotNull(message = "id do funcionário que abriu o caixa é obrigatório")
    private Long employeeId;

}
