package com.new_big.web.controller.batch.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BatchRequest {

    @NotBlank(message = "códego de lote é obrigatório")
    private String batchCode;

    @NotNull
    @Positive
    private int quantity;

    @NotNull
    private Long productId;

    @Future(message = "lote deve ter uma validade no futuro")
    private LocalDate expirationAt;

}
