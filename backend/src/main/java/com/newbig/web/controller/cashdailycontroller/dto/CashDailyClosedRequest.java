package com.newbig.web.controller.cashdailycontroller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CashDailyClosedRequest {

    @NotNull(message = "Valor de fechamento de caixa é obrigatório")
    @PositiveOrZero(message = "O valor de fechamento não pode ser negativo")
    private BigDecimal closingAmount;

    @NotNull(message = "id do funcionário que fechou o caixa é obrigatório")
    private Long employeeId;
}
