package com.newbig.web.service;

import com.newbig.web.controller.batch.dto.BatchConsumption;
import com.newbig.web.entity.Batch;
import com.newbig.web.entity.Sale;
import com.newbig.web.entity.StockMovement;
import com.newbig.web.enums.MovementType;
import com.newbig.web.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository repository;
    private final BatchService batchService;

    public List<StockMovement> saveMovementSale(List<BatchConsumption> consumption, Sale sale) {
        log.info("Inicializando lançamento das movimentações da venda");

        List<StockMovement> list = new ArrayList<>();

        for (BatchConsumption c : consumption) {
            Batch batch = this.batchService.findById(c.batchId());
            StockMovement s = new StockMovement();
            s.setObservation("Movimentação vinda da venda Id: " + sale.getId());
            s.setQuantity(c.quantityUsed());
            s.setMovementDate(LocalDateTime.now());
            s.setType(MovementType.SALE_EXIT);
            s.setBatch(batch);
            s.setEmployee(sale.getEmployee());
            s.setSale(sale);
            list.add(s);
        }

        for (StockMovement s : list) this.repository.save(s);
        log.info("Lançamentos de venda feitos com sucesso!");
        return list;
    }
}
