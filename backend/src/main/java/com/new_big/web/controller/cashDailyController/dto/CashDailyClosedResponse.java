package com.new_big.web.controller.cashDailyController.dto;

import com.new_big.web.entity.CashDailyLog;
import com.new_big.web.enums.CashStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashDailyClosedResponse(
        Long id,
        BigDecimal openingAmount,
        BigDecimal closingAmount,
        BigDecimal expectedClosingAmount,
        BigDecimal total,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        String openedBy,
        String closedBy,

        @Enumerated(EnumType.STRING)
        CashStatus status
) {
    public static CashDailyClosedResponse de(CashDailyLog cash){
        return new CashDailyClosedResponse(
                cash.getId(),
                cash.getOpeningAmount(),
                cash.getClosingAmount(),
                cash.getExpectedClosingAmount(),
                cash.getTotal(),
                cash.getOpenedAt(),
                cash.getClosedAt(),
                cash.getOpenedBy().getName(),
                cash.getClosedBy() != null
                        ? cash.getClosedBy().getName()
                        : null,
                cash.getStatus()
        );
    }
}
