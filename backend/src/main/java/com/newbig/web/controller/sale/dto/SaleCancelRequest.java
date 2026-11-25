package com.newbig.web.controller.sale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaleCancelRequest(
        @NotNull @Positive Long cashId
) {
}
