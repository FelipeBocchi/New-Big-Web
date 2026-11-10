package com.new_big.web.controller.saleController.dto;

import com.new_big.web.entity.Sale;
import com.new_big.web.enums.SaleStatus;

import java.math.BigDecimal;

public record SaleOpenResponse(
        Long id,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        SaleStatus status,
        String customerName,
        String employeeName
) {
    public static SaleOpenResponse de(Sale sale){
        return new SaleOpenResponse(
                sale.getId(),
                sale.getSubtotal(),
                sale.getDiscount(),
                sale.getTotal(),
                sale.getStatus(),
                sale.getCustomer() != null
                        ? sale.getCustomer().getName()
                        : null,
                sale.getEmployee().getName()
        );
    }
}
