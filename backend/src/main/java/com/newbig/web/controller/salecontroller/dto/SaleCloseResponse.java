package com.newbig.web.controller.salecontroller.dto;

import com.newbig.web.entity.Sale;
import com.newbig.web.enums.PaymentMethod;
import com.newbig.web.enums.SaleStatus;

import java.math.BigDecimal;

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
