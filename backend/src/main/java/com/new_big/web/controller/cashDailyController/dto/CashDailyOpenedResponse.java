package com.new_big.web.controller.cashDailyController.dto;

import com.new_big.web.entity.CashDailyLog;
import com.new_big.web.enums.CashStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashDailyOpenedResponse(
        Long id,
        BigDecimal openingAmount,
        BigDecimal total,
        LocalDateTime openedAt,
        String nameOfEmployee,

        @Enumerated(EnumType.STRING)
        CashStatus status

) {
    public static CashDailyOpenedResponse de(CashDailyLog request) {
        return new CashDailyOpenedResponse(
                request.getId(),
                request.getOpeningAmount(),
                request.getTotal(),
                request.getOpenedAt(),
                request.getOpenedBy().getName(),
                request.getStatus()
        );
    }
}
