package com.new_big.web.controller.batch.dto;

import com.new_big.web.entity.Batch;

import java.time.LocalDate;

public record BatchResponse(
        Long id,
        String batchCode,
        int quantity,
        Boolean expiration,
        Boolean quantityIsZero,
        LocalDate createdAt,
        LocalDate expirationAt,
        //Long productId  removi esse pq é melhor para o front retornar o nome do produto do que o id
        String productName
) {
    public static BatchResponse de(Batch batch) {
        return new BatchResponse(
                batch.getId(),
                batch.getBatchCode(),
                batch.getQuantity(),
                batch.getExpiration(),
                batch.getQuantityIsZero(),
                batch.getCreatedAt(),
                batch.getExpirationAt(),
                batch.getProduct().getName()
        );
    }
}
