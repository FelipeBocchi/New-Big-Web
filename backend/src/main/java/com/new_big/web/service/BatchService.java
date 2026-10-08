package com.new_big.web.service;

import com.new_big.web.controller.batch.dto.BatchRequest;
import com.new_big.web.controller.product.dto.ProductRequest;
import com.new_big.web.entity.Batch;
import com.new_big.web.entity.Product;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.BatchRepository;
import com.new_big.web.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchRepository repository;
    private final ProductRepository productRepository;

    @Transactional
    public Batch save(BatchRequest request) {

        log.info("Criando novo lote. barcode={}", request.getBatchCode());

        if (this.repository.existsByBatchCode(request.getBatchCode())) {
            log.warn("Cadastro rejeitado: Barcode já existente ({})", request.getBatchCode());
            throw new ConflictException("Já existe um lote cadastrado com este barcode");
        }

        Batch batch = new Batch();
        updateFields(batch, request);
        batch.setExpiration(false); // vamos inicializar como false pq não vai permitir uma data no passado
        batch.setQuantityIsZero(false);

        Batch saved = this.repository.save(batch);
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
        Batch batch = this.findById(id);

        if (request.getBatchCode() != null) batch.setBatchCode(request.getBatchCode());
        //if (request.getQuantity() != null) batch.setQuantity(request.getQuantity());
        if (request.getExpirationAt() != null) batch.setExpirationAt(request.getExpirationAt());
        if (request.getProductId() != null) {
            Product product = this.productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com na hora de criar um lote "));

            batch.setProduct(product);
        }

        return this.repository.save(batch);
    }


    @Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void toggleExpiration() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        log.info("Iniciando verificação de validade dos lotes. Data: {}", today);
        List<Batch> batchList = this.findAll();

        for (Batch b : batchList) {

            LocalDate expirationAt = b.getExpirationAt();

            if (!expirationAt.isAfter(today) && Boolean.TRUE.equals(b.getExpiration())) {

                b.setExpiration(true);
                this.repository.save(b);

                log.info("Lote {} marcado como expirado. Vencimento: {}", b.getBatchCode(), expirationAt);
            }
        }
        log.info("Verificação diária de validade dos lotes concluída.");
    }

    public void updateFields(Batch batch, BatchRequest request) {
        Product product = this.productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com na hora de criar um lote "));

        batch.setBatchCode(request.getBatchCode());
        batch.setCreatedAt(LocalDate.now());
        batch.setExpirationAt(request.getExpirationAt());
        batch.setQuantity(request.getQuantity());
        batch.setProduct(product);

    }
}
