package com.newbig.web.controller.batch.dto;

public record BatchConsumption(
        Long batchId,
        String batchCode,
        int quantityUsed
) {
}
