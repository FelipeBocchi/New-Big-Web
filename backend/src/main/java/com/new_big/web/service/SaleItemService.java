package com.new_big.web.service;

import com.new_big.web.controller.batch.dto.BatchConsumption;
import com.new_big.web.entity.Product;
import com.new_big.web.entity.Sale;
import com.new_big.web.entity.SaleItem;
import com.new_big.web.repository.SaleItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleItemService {

    private final SaleItemRepository repository;

    public List<SaleItem> save(List<BatchConsumption> consumptions, Product product, Sale sale) {
        log.info("Inicializando o cadastro dos itens da venda");

        List<SaleItem> list = new ArrayList<>();

        for (BatchConsumption c : consumptions) {
            BigDecimal total = product.getSalePrice().multiply(BigDecimal.valueOf(c.quantityUsed()));
            SaleItem s = new SaleItem();
            s.setQuantity(c.quantityUsed());
            s.setUnitPrice(product.getSalePrice());
            s.setTotal(total);
            s.setProduct(product);
            s.setSale(sale);
            list.add(s);
        }

        for (SaleItem i : list) this.repository.save(i);
        log.info("Cadastro de itens da venda feito com sucesso!");
        return list;

    }

}
