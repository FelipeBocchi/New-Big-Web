package com.new_big.web.controller.cashDailyController.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CashDailyClosedRequest {

    @NotNull(message = "Valor de fechamento de caixa é obrigatório")
    private BigDecimal closingAmount;

    @NotNull(message = "id do funcionário que fechou o caixa é obrigatório")
    private Long employeeId;
}
