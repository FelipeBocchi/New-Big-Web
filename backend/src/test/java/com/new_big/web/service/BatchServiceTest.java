package com.new_big.web.service;

import com.new_big.web.controller.batch.dto.BatchRequest;
import com.new_big.web.entity.Batch;
import com.new_big.web.entity.Product;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.BatchRepository;
import com.new_big.web.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private BatchRepository repository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BatchService service;

    private Product product;
    private Batch batch;
    private BatchRequest request;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Coca-Cola");

        batch = new Batch();
        batch.setId(1L);
        batch.setBatchCode("LOTE001");
        batch.setQuantity(10);
        batch.setExpiration(false);
        batch.setQuantityIsZero(false);
        batch.setCreatedAt(LocalDate.now());
        batch.setExpirationAt(LocalDate.now().plusDays(10));
        batch.setProduct(product);

        request = new BatchRequest();
        request.setBatchCode("LOTE001");
        request.setQuantity(10);
        request.setProductId(1L);
        request.setExpirationAt(LocalDate.now().plusDays(10));
    }

    @Test
    void saveDeveCadastrarLote() {
        when(repository.existsByBatchCode("LOTE001")).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any(Batch.class))).thenAnswer(invocation -> {
            Batch saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Batch result = service.save(request);

        assertNotNull(result);
        assertEquals("LOTE001", result.getBatchCode());
        assertEquals(10, result.getQuantity());
        assertFalse(result.getExpiration());
        assertFalse(result.getQuantityIsZero());
        assertEquals(product, result.getProduct());

        verify(repository).save(any(Batch.class));
    }

    @Test
    void saveDeveLancarConflitoQuandoCodigoJaExiste() {
        when(repository.existsByBatchCode("LOTE001")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.save(request));

        verify(repository, never()).save(any(Batch.class));
    }

    @Test
    void saveDeveFalharQuandoProdutoNaoExiste() {
        when(repository.existsByBatchCode("LOTE001")).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.save(request));

        verify(repository, never()).save(any(Batch.class));
    }

    @Test
    void findByIdDeveRetornarLote() {
        when(repository.findById(1L)).thenReturn(Optional.of(batch));

        Batch result = service.findById(1L);

        assertEquals(batch, result);
        assertEquals("LOTE001", result.getBatchCode());
    }

    @Test
    void findByIdDeveFalharQuandoLoteNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(99L));
    }

    @Test
    void findByExpirationDeveRetornarLotes() {
        when(repository.findByExpiration(false)).thenReturn(List.of(batch));

        List<Batch> result = service.findByExpiration(false);

        assertEquals(1, result.size());
        assertEquals(batch, result.get(0));
    }

    @Test
    void findByExpirationDeveFalharQuandoListaVazia() {
        when(repository.findByExpiration(true)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findByExpiration(true));
    }

    @Test
    void findAllDeveRetornarTodosOsLotes() {
        when(repository.findAll()).thenReturn(List.of(batch));

        List<Batch> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals(batch, result.get(0));
    }

    @Test
    void updateDeveAtualizarLote() {
        request.setBatchCode("LOTE002");

        when(repository.findById(1L)).thenReturn(Optional.of(batch));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any(Batch.class))).thenReturn(batch);

        Batch result = service.update(request, 1L);

        assertNotNull(result);
        assertEquals("LOTE002", result.getBatchCode());
        verify(repository).save(batch);
    }

    @Test
    void updateDeveFalharQuandoCodigoPertenceAOutroLote() {
        request.setBatchCode("LOTE002");

        when(repository.findById(1L)).thenReturn(Optional.of(batch));
        when(repository.existsByBatchCode("LOTE002")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.update(request, 1L));

        verify(repository, never()).save(any(Batch.class));
    }

    @Test
    void updateDeveFalharQuandoProdutoNaoExiste() {
        request.setBatchCode("LOTE001");

        when(repository.findById(1L)).thenReturn(Optional.of(batch));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(request, 1L));
    }

    @Test
    void updatePartialDeveAtualizarCodigoEValidade() {
        LocalDate newExpiration = LocalDate.now().plusDays(20);

        BatchRequest partialRequest = new BatchRequest();
        partialRequest.setBatchCode("LOTE003");
        partialRequest.setExpirationAt(newExpiration);

        when(repository.findById(1L)).thenReturn(Optional.of(batch));
        when(repository.save(any(Batch.class))).thenReturn(batch);

        service.updatePartial(partialRequest, 1L);

        assertEquals("LOTE003", batch.getBatchCode());
        assertEquals(newExpiration, batch.getExpirationAt());

        verify(repository).save(batch);
        verifyNoInteractions(productRepository);
    }

    @Test
    void updatePartialDeveAtualizarProduto() {
        Product anotherProduct = new Product();
        anotherProduct.setId(2L);
        anotherProduct.setName("Guaraná");

        BatchRequest partialRequest = new BatchRequest();
        partialRequest.setProductId(2L);

        when(repository.findById(1L)).thenReturn(Optional.of(batch));
        when(productRepository.findById(2L))
                .thenReturn(Optional.of(anotherProduct));
        when(repository.save(any(Batch.class))).thenReturn(batch);

        service.updatePartial(partialRequest, 1L);

        assertEquals(anotherProduct, batch.getProduct());
        verify(repository).save(batch);
    }

    @Test
    void updatePartialDeveFalharQuandoProdutoNaoExiste() {
        BatchRequest partialRequest = new BatchRequest();
        partialRequest.setProductId(99L);

        when(repository.findById(1L)).thenReturn(Optional.of(batch));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updatePartial(partialRequest, 1L));

        verify(repository, never()).save(any(Batch.class));
    }

    @Test
    void updatePartialDeveFalharQuandoLoteNaoExiste() {
        Long idInexistente = 99L;

        when(repository.findById(idInexistente)).thenReturn(Optional.empty());
        BatchRequest request = new BatchRequest();

        assertThrows(ResourceNotFoundException.class,
                () -> service.updatePartial(request, idInexistente));

        verify(repository, never()).save(any(Batch.class));
    }

    @Test
    void toggleExpirationDeveSalvarLoteVencidoMarcadoComoExpirado() {
        batch.setExpiration(true);
        batch.setExpirationAt(LocalDate.now().minusDays(1));

        when(repository.findAll()).thenReturn(List.of(batch));
        when(repository.save(any(Batch.class))).thenReturn(batch);

        service.toggleExpiration();

        assertTrue(batch.getExpiration());
        verify(repository).save(batch);
    }

    @Test
    void toggleExpirationNaoDeveSalvarLoteAindaNaoVencido() {
        batch.setExpiration(true);
        batch.setExpirationAt(LocalDate.now().plusDays(10));

        when(repository.findAll()).thenReturn(List.of(batch));

        service.toggleExpiration();

        verify(repository, never()).save(any(Batch.class));
    }

    @Test
    void toggleExpirationDeveIgnorarLoteNaoMarcadoComoExpirado() {
        batch.setExpiration(false);
        batch.setExpirationAt(LocalDate.now().minusDays(1));

        when(repository.findAll()).thenReturn(List.of(batch));

        service.toggleExpiration();

        verify(repository, never()).save(any(Batch.class));
    }
}