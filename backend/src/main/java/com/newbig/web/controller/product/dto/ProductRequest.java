package com.newbig.web.controller.product.dto;

import com.newbig.web.config.validation.CompleteRequest;
import com.newbig.web.enums.ProductType;
import com.newbig.web.enums.UnitType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(
            groups = CompleteRequest.class, message = "Nome é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    private String name;

    @NotBlank(
            groups = CompleteRequest.class, message = "Description é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    private String description;

    @NotBlank(
            groups = CompleteRequest.class, message = "Category é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    private String category;

    @NotBlank(message = "Barcode é obrigatório")
    @Size(max = 3, message = "Barcode deve ter no máximo 3 caracteres")
    private String barcode;

    @NotNull(groups = CompleteRequest.class)
    @Positive
    private Integer minimumStock;

    @NotNull(groups = CompleteRequest.class)
    private Boolean active;

    @NotNull(groups = CompleteRequest.class)
    @Positive
    private BigDecimal salePrice;

    @NotNull(groups = CompleteRequest.class)
    @Positive
    private BigDecimal costPrice;

    @Enumerated(EnumType.STRING)
    private UnitType unitType;

    @Enumerated(EnumType.STRING)
    private ProductType productType;
}
