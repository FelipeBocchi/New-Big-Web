package com.newbig.web.controller.batch.dto;

import com.newbig.web.config.validation.BatchCreation;
import com.newbig.web.config.validation.CompleteRequest;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BatchRequest {

    @NotBlank(
            groups = CompleteRequest.class, message = "Nome é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    private String batchCode;

    @NotNull(groups = CompleteRequest.class)
    @Positive
    private Integer quantity;

    @NotNull(groups = CompleteRequest.class)
    private Long productId;

    @NotNull(
            groups = BatchCreation.class,
            message = "Informe o funcionário responsável pela entrada"
    )
    @Positive
    private Long employeeId;

    @NotNull(groups = CompleteRequest.class)
    @Future(message = "Lote deve ter uma validade no futuro")
    private LocalDate expirationAt;

}
