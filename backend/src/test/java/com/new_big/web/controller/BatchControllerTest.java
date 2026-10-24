package com.new_big.web.controller;

import com.new_big.web.controller.batch.BatchController;
import com.new_big.web.controller.batch.dto.BatchRequest;
import com.new_big.web.entity.Batch;
import com.new_big.web.entity.Product;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.GlobalExceptionHandler;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.service.BatchService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatchController.class)
@Import(GlobalExceptionHandler.class)
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatchService batchService;

    private Batch criarLote() {
        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Coca-Cola");

        Batch batch = new Batch();
        batch.setId(1L);
        batch.setBatchCode("LOTE001");
        batch.setQuantity(100);
        batch.setExpiration(false);
        batch.setQuantityIsZero(false);
        batch.setCreatedAt(LocalDate.now());
        batch.setExpirationAt(LocalDate.now().plusDays(30));
        batch.setProduct(produto);

        return batch;
    }

    private String jsonLote() {
        return """
                {
                    "batchCode": "LOTE001",
                    "quantity": 100,
                    "productId": 1,
                    "expirationAt": "%s"
                }
                """.formatted(LocalDate.now().plusDays(30));
    }

    @Test
    void deveCadastrarLote() throws Exception {
        when(batchService.save(any(BatchRequest.class)))
                .thenReturn(criarLote());

        mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLote()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.batchCode").value("LOTE001"))
                .andExpect(jsonPath("$.productName").value("Coca-Cola"));

        verify(batchService).save(any(BatchRequest.class));
    }

    @Test
    void deveRetornarLotePorId() throws Exception {
        when(batchService.findById(1L)).thenReturn(criarLote());

        mockMvc.perform(get("/api/batch/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.batchCode").value("LOTE001"));

        verify(batchService).findById(1L);
    }

    @Test
    void deveRetornarErroQuandoLoteNaoExistir() throws Exception {
        when(batchService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Lote não encontrado"));

        mockMvc.perform(get("/api/batch/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(batchService).findById(99L);
    }

    @Test
    void deveListarLotes() throws Exception {
        when(batchService.findAll()).thenReturn(List.of(criarLote()));

        mockMvc.perform(get("/api/batch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].batchCode").value("LOTE001"))
                .andExpect(jsonPath("$[0].productName").value("Coca-Cola"));

        verify(batchService).findAll();
    }

    @Test
    void deveListarLotesPorValidade() throws Exception {
        when(batchService.findByExpiration(false))
                .thenReturn(List.of(criarLote()));

        mockMvc.perform(get("/api/batch/expiration/{expiration}", false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].batchCode").value("LOTE001"))
                .andExpect(jsonPath("$[0].expiration").value(false));

        verify(batchService).findByExpiration(false);
    }

    @Test
    void deveRetornarErroQuandoNaoHouverLotesComValidade() throws Exception {
        when(batchService.findByExpiration(true))
                .thenThrow(new ResourceNotFoundException("Nenhum lote encontrado"));

        mockMvc.perform(get("/api/batch/expiration/{expiration}", true))
                .andExpect(status().isNotFound());

        verify(batchService).findByExpiration(true);
    }

    @Test
    void deveAtualizarLote() throws Exception {
        when(batchService.update(any(BatchRequest.class), eq(1L)))
                .thenReturn(criarLote());

        mockMvc.perform(put("/api/batch/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLote()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.batchCode").value("LOTE001"));

        verify(batchService).update(any(BatchRequest.class), eq(1L));
    }

    @Test
    void deveAtualizarLoteParcialmente() throws Exception {
        when(batchService.updatePartial(any(BatchRequest.class), eq(1L)))
                .thenReturn(criarLote());

        mockMvc.perform(patch("/api/batch/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLote()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.batchCode").value("LOTE001"));

        verify(batchService).updatePartial(any(BatchRequest.class), eq(1L));
    }

    @Test
    void deveRetornarErroQuandoDadosForemInvalidos() throws Exception {
        String jsonInvalido = """
                {
                    "batchCode": "",
                    "quantity": -1,
                    "productId": null,
                    "expirationAt": "2000-01-01"
                }
                """;

        mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(batchService);
    }

    @Test
    void deveRetornarConflitoAoCadastrarLoteDuplicado() throws Exception {
        when(batchService.save(any(BatchRequest.class)))
                .thenThrow(new ConflictException("Lote já cadastrado"));

        mockMvc.perform(post("/api/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLote()))
                .andExpect(status().isConflict());

        verify(batchService).save(any(BatchRequest.class));
    }
}