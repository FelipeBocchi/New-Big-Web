package com.new_big.web.controller.saleController.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleOpenRequest {

    private Long customerId;

    @NotNull(message = "id do funcionário que abriu o caixa é obrigatório")
    private Long employeeId;

    @NotNull(message = "id do funcionário que abriu o caixa é obrigatório")
    private Long cashId;

}
