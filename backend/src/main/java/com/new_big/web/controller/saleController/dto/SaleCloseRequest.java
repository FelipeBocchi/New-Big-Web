package com.new_big.web.controller.saleController.dto;

import com.new_big.web.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SaleCloseRequest {

    @NotNull(message = "Id do caixa é necessário para fechar")
    private Long cashId;

    @Positive
    @NotNull(message = "No pagamento deve ser recebido um valor do cliente")
    private BigDecimal valueArrived;

    private BigDecimal discount;

    @NotNull(message = "método de pagamento deve ser informada ao sistema")
    private PaymentMethod paymentMethod;


}
