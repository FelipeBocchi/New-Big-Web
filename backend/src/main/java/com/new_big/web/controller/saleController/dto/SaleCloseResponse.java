package com.new_big.web.controller.saleController.dto;

import com.new_big.web.entity.Sale;
import com.new_big.web.enums.PaymentMethod;
import com.new_big.web.enums.SaleStatus;

import java.math.BigDecimal;
import java.util.List;

public record SaleCloseResponse(
        Long id,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        SaleStatus status,
        PaymentMethod paymentMethod,
        String customerName,
        String employeeName
) {
    public static SaleCloseResponse de(Sale sale) {
        return new SaleCloseResponse(
                sale.getId(),
                sale.getSubtotal(),
                sale.getDiscount(),
                sale.getTotal(),
                sale.getStatus(),
                sale.getPaymentMethod(),
                sale.getCustomer() != null
                        ? sale.getCustomer().getName()
                        : null,
                sale.getEmployee().getName()
        );
    }
}
