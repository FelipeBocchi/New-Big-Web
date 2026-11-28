package com.newbig.web.service;

import com.newbig.web.controller.batch.dto.BatchConsumption;
import com.newbig.web.entity.*;
import com.newbig.web.enums.MovementType;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.BatchRepository;
import com.newbig.web.repository.EmployeeRepository;
import com.newbig.web.repository.StockMovementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository repository;
    private final BatchRepository batchRepository;
    private final EmployeeRepository employeeRepository;
    private final Clock clock;

    @Transactional
    public StockMovement saveMovementEntry( Batch batch, Long employeeId) {

        if (batch == null
                || batch.getId() == null
                || batch.getQuantity() <= 0) {
            throw new ConflictException(
                    "A entrada exige um lote salvo e quantidade positiva"
            );
        }

        if (employeeId == null || employeeId <= 0) {
            throw new ConflictException(
                    "Informe o funcionário responsável pela entrada"
            );
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Funcionário não encontrado com o ID: "
                                        + employeeId
                        )
                );

        StockMovement movement = new StockMovement();
        movement.setObservation(
                "Entrada do lote: " + batch.getBatchCode()
        );
        movement.setQuantity(batch.getQuantity());
        movement.setMovementDate(LocalDateTime.now(clock));
        movement.setType(MovementType.ENTRY);
        movement.setBatch(batch);
        movement.setEmployee(employee);
        movement.setSale(null);

        // A quantidade inicial já está no lote.
        // Aqui apenas registramos o histórico da entrada.
        return repository.save(movement);
    }

    @Transactional
    public List<StockMovement> saveMovementSale( List<BatchConsumption> consumption, Sale sale) {
        if (sale == null
                || sale.getId() == null
                || sale.getEmployee() == null
                || sale.getEmployee().getId() == null) {
            throw new ConflictException(
                    "A saída exige venda e funcionário identificados"
            );
        }

        if (consumption == null) {
            throw new ConflictException(
                    "Informe os consumos da venda"
            );
        }

        // Valida todos os consumos antes de consultar ou gravar.
        for (BatchConsumption item : consumption) {
            if (item == null
                    || item.batchId() == null
                    || item.batchId() <= 0
                    || item.quantityUsed() <= 0) {
                throw new ConflictException(
                        "Cada consumo deve informar lote válido e quantidade positiva"
                );
            }
        }

        List<StockMovement> movements = new ArrayList<>();

        for (BatchConsumption item : consumption) {
            Batch batch = batchRepository.findById(item.batchId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Lote não encontrado com o ID: "
                                            + item.batchId()
                            )
                    );

            StockMovement movement = new StockMovement();
            movement.setObservation(
                    "Movimentação vinda da venda Id: " + sale.getId()
            );
            movement.setQuantity(item.quantityUsed());
            movement.setMovementDate(LocalDateTime.now(clock));
            movement.setType(MovementType.SALE_EXIT);
            movement.setBatch(batch);
            movement.setEmployee(sale.getEmployee());
            movement.setSale(sale);

            movements.add(movement);
        }

        for (StockMovement movement : movements) {
            repository.save(movement);
        }

        return movements;
    }

    @Transactional
    public void reverseSale(Sale sale, Long employeeId) {
        Map<Long, Integer> quantities = sale.getStockMovements().stream()
                .filter(movement -> movement.getType() == MovementType.SALE_EXIT)
                .collect(Collectors.toMap(movement -> movement.getBatch().getId(), StockMovement::getQuantity, Integer::sum));

        if (quantities.isEmpty()) {
            return;
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado"));
        List<Batch> batches = batchRepository.findByIdsForUpdate(quantities.keySet());
        if (batches.size() != quantities.size()) {
            throw new ConflictException("Não foi possível localizar todos os lotes da venda");
        }

        for (Batch batch : batches) {
            int quantity = quantities.get(batch.getId());
            batch.setQuantity(batch.getQuantity() + quantity);
            batch.setQuantityIsZero(false);
            StockMovement reversal = new StockMovement();
            reversal.setObservation("Estorno da venda #" + sale.getId());
            reversal.setType(MovementType.ADJUSTMENT);
            reversal.setQuantity(quantity);
            reversal.setMovementDate(LocalDateTime.now(clock));
            reversal.setBatch(batch);
            reversal.setEmployee(employee);
            sale.addStockMovement(reversal);
            repository.save(reversal);
        }
    }
}