package com.newbig.web.service;

import com.newbig.web.controller.batch.dto.BatchConsumption;
import com.newbig.web.controller.batch.dto.BatchRequest;
import com.newbig.web.entity.Batch;
import com.newbig.web.entity.Product;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.BatchRepository;
import com.newbig.web.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.time.Clock;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchRepository repository;
    private final ProductRepository productRepository;
    private final StockMovementService movementService;
    private final Clock clock;

    @Transactional
    public Batch save(BatchRequest request) {

        log.info("Criando novo lote. barcode={}", request.getBatchCode());

        if (this.repository.existsByBatchCode(request.getBatchCode())) {
            log.warn("Cadastro rejeitado: Barcode já existente ({})", request.getBatchCode());
            throw new ConflictException("Já existe um lote cadastrado com este barcode");
        }

        Batch batch = new Batch();
        updateFields(batch, request);
        batch.setCreatedAt(LocalDate.now(clock));
        batch.setExpiration(false);
        batch.setQuantityIsZero(false);

        Batch saved = repository.save(batch);

        movementService.saveMovementEntry(
                saved,
                request.getEmployeeId()
        );

        log.info("Lote criado com sucesso. barcode={}", saved.getBatchCode());
        return saved;
    }

    public Batch findById(Long id) {

        return this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado com o ID: \" + id"));
    }

    public List<Batch> findByExpiration(Boolean expiration) {
        List<Batch> list = this.repository.findByExpiration(expiration);

        if (list.isEmpty()) throw new ResourceNotFoundException("Nemnhum lote com a validade");

        return list;
    }

    public List<Batch> findAll() { return this.repository.findAll(); }

    @Transactional
    public Batch update(BatchRequest request, Long id) {
        log.info("Atualizando lote. id={}", id);
        Batch batch = findById(id);

        if (!batch.getBatchCode().equals(request.getBatchCode()) && repository.existsByBatchCode(request.getBatchCode())) {
            throw new ConflictException("Já existe outro lote cadastrado com este Barcode");
        }

        updateFields(batch, request);
        log.info("Lote atualizado com sucesso. id={}", id);
        return repository.save(batch);
    }


    @Transactional
    public Batch updatePartial(BatchRequest request, Long id) {
        log.info("Atualizando produto. id={}", id);
        Batch batch = findById(id);

        if (request.getBatchCode() != null
                && !request.getBatchCode().equals(batch.getBatchCode())
                && repository.existsByBatchCode(request.getBatchCode())) {
            throw new ConflictException(
                    "Já existe outro lote cadastrado com este código"
            );
        }

        // Valida antes de alterar os campos.
        if (request.getQuantity() != null) {
            validateQuantity(request.getQuantity());
        }

        if (request.getExpirationAt() != null) {
            validateExpiration(request.getExpirationAt());
        }

        Product product = null;

        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Produto não encontrado para o lote"
                            )
                    );
        }

        if (request.getBatchCode() != null) {
            batch.setBatchCode(request.getBatchCode());
        }

        if (request.getQuantity() != null) {
            batch.setQuantity(request.getQuantity());
            batch.setQuantityIsZero(false);
        }

        if (request.getExpirationAt() != null) {
            batch.setExpirationAt(request.getExpirationAt());
            batch.setExpiration(false);
        }

        if (product != null) {
            batch.setProduct(product);
        }

        return repository.save(batch);
    }

    @Scheduled(
            cron = "0 0 0 * * *",
            zone = "America/Sao_Paulo"
    )
    @Transactional
    public void toggleExpiration() {
        log.info("Iniciando verificação de validade dos lotes. Data");
        LocalDate today = LocalDate.now(clock);

        for (Batch batch : repository.findAll()) {
            LocalDate expirationAt = batch.getExpirationAt();

            if (expirationAt != null
                    && !expirationAt.isAfter(today)
                    && !Boolean.TRUE.equals(batch.getExpiration())) {

                batch.setExpiration(true);
                repository.save(batch);
                log.info("Lote {} marcado como expirado. Vencimento: {}", batch.getBatchCode(), expirationAt);
            }
        }
        log.info("Verificação diária de validade dos lotes concluída.");
    }

    public void updateFields(Batch batch, BatchRequest request) {
        validateQuantity(request.getQuantity());
        validateExpiration(request.getExpirationAt());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Produto não encontrado para o lote"
                        )
                );

        batch.setBatchCode(request.getBatchCode());
        batch.setExpirationAt(request.getExpirationAt());
        batch.setQuantity(request.getQuantity());
        batch.setProduct(product);

        // As validações acima exigem validade futura e quantidade positiva.
        batch.setExpiration(false);
        batch.setQuantityIsZero(false);
    }

    public List<BatchConsumption> fefo(Long productId, int quantity) {
        log.info("inicializando o FEFO");
        validateQuantity(quantity);

        // Buca direto do repository do lote para não perdemos tempo com ordenamento ou pexando um lista enorme de lote
        List<Batch> batches = this.repository.findAvailableBatchesFefo(productId, LocalDate.now());

        long availableQuantity = batches.stream()
                .mapToLong(Batch::getQuantity)
                .sum();

        if (availableQuantity < quantity) { throw new ConflictException("Estoque insuficiente para o produto ID: " + productId); }

        List<BatchConsumption> consumptions = new ArrayList<>();
        int remaining = quantity;

        for (Batch batch : batches) {
            if (remaining == 0) {
                break;
            }

            int quantityToConsume = Math.min(
                    batch.getQuantity(),
                    remaining
            );

            int newQuantity = batch.getQuantity() - quantityToConsume;

            batch.setQuantity(newQuantity);
            batch.setQuantityIsZero(newQuantity == 0);

            consumptions.add(
                    new BatchConsumption(
                            batch.getId(),
                            batch.getBatchCode(),
                            quantityToConsume
                    )
            );

            remaining -= quantityToConsume;
        }

        return consumptions;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ConflictException(
                    "A quantidade deve ser maior que zero"
            );
        }
    }

    private void validateExpiration(LocalDate expiration) {
        if (expiration == null
                || !expiration.isAfter(LocalDate.now(clock))) {
            throw new ConflictException(
                    "A validade deve estar no futuro"
            );
        }
    }
}
