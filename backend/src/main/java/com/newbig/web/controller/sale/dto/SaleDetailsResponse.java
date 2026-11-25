package com.newbig.web.controller.sale.dto;

import com.newbig.web.entity.Sale;
import com.newbig.web.entity.SaleItem;
import com.newbig.web.enums.PaymentMethod;
import com.newbig.web.enums.SaleStatus;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record SaleDetailsResponse(
        Long id,
        Long cashId,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        SaleStatus status,
        PaymentMethod paymentMethod,
        String customerName,
        String employeeName,
        List<Item> items
) {
    public static SaleDetailsResponse de(Sale sale) {
        SaleCloseResponse summary = SaleCloseResponse.de(sale);
        List<Item> items = sale.getSaleItems().stream()
                .sorted(Comparator.comparing(SaleItem::getId)).map(Item::de).toList();
        return new SaleDetailsResponse(
                summary.id(),
                summary.cashId(),
                summary.subtotal(),
                summary.discount(),
                summary.total(),
                summary.status(),
                summary.paymentMethod(),
                summary.customerName(),
                summary.employeeName(),
                items);
    }

    public record Item(Long id, String productName, BigDecimal unitPrice, int quantity, BigDecimal total) {
        private static Item de(SaleItem item) {
            return new Item(
                    item.getId(),
                    item.getProduct().getName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getTotal());
        }
    }
}
