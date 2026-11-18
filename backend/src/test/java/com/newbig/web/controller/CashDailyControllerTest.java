package com.newbig.web.controller;

import com.newbig.web.controller.cashdailycontroller.CashDailyController;
import com.newbig.web.controller.cashdailycontroller.dto.CashDailyClosedRequest;
import com.newbig.web.controller.cashdailycontroller.dto.CashDailyOpenedRequest;
import com.newbig.web.entity.CashDailyLog;
import com.newbig.web.entity.Employee;
import com.newbig.web.enums.CashStatus;
import com.newbig.web.exception.GlobalExceptionHandler;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.service.CashDailyService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CashDailyController.class)
@Import(GlobalExceptionHandler.class)
class CashDailyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CashDailyService cashDailyService;

    private CashDailyLog criarCaixa() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("João Funcionário");

        CashDailyLog cash = new CashDailyLog();
        cash.setId(1L);
        cash.setOpeningAmount(BigDecimal.valueOf(100.00));
        cash.setClosingAmount(BigDecimal.valueOf(500.00));
        cash.setExpectedClosingAmount(BigDecimal.valueOf(500.00));
        cash.setTotal(BigDecimal.valueOf(400.00));
        cash.setOpenedAt(LocalDateTime.of(2023, 10, 10, 8, 0));
        cash.setClosedAt(LocalDateTime.of(2023, 10, 10, 18, 0));
        cash.setOpenedBy(employee);
        cash.setClosedBy(employee);
        cash.setStatus(CashStatus.OPEN);

        return cash;
    }

    private String jsonCashOpenedRequest() {
        return """
                {
                    "openingAmount": 100.00,
                    "employeeId": 1
                }
                """;
    }

    private String jsonCashClosedRequest() {
        return """
                {
                    "closingAmount": 500.00,
                    "employeeId": 1
                }
                """;
    }

    @Test
    void deveAbrirCaixaComSucesso() throws Exception {
        when(cashDailyService.open(any(CashDailyOpenedRequest.class))).thenReturn(criarCaixa());

        mockMvc.perform(post("/api/cash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCashOpenedRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.openingAmount").value(100.00))
                .andExpect(jsonPath("$.nameOfEmployee").value("João Funcionário"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(cashDailyService).open(any(CashDailyOpenedRequest.class));
    }

    @Test
    void deveRetornarErroAoAbrirCaixaComDadosInvalidos() throws Exception {
        String jsonInvalido = """
                {
                    "openingAmount": -50.00,
                    "employeeId": null
                }
                """;

        mockMvc.perform(post("/api/cash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cashDailyService);
    }

    @Test
    void deveFecharCaixaComSucesso() throws Exception {
        CashDailyLog caixaFechado = criarCaixa();
        caixaFechado.setStatus(CashStatus.CLOSED);

        when(cashDailyService.close(any(CashDailyClosedRequest.class), eq(1L))).thenReturn(caixaFechado);

        mockMvc.perform(patch("/api/cash/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCashClosedRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.closingAmount").value(500.00))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.closedBy").value("João Funcionário"));

        verify(cashDailyService).close(any(CashDailyClosedRequest.class), eq(1L));
    }

    @Test
    void deveRetornarErroAoFecharCaixaComDadosInvalidos() throws Exception {
        String jsonInvalido = """
                {
                    "closingAmount": null,
                    "employeeId": null
                }
                """;

        mockMvc.perform(patch("/api/cash/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cashDailyService);
    }

    @Test
    void deveListarTodosOsCaixas() throws Exception {
        when(cashDailyService.findAll()).thenReturn(List.of(criarCaixa()));

        mockMvc.perform(get("/api/cash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].openingAmount").value(100.00))
                .andExpect(jsonPath("$[0].openedBy").value("João Funcionário"));

        verify(cashDailyService).findAll();
    }

    @Test
    void deveRetornarInfoDoCaixaPorId() throws Exception {
        when(cashDailyService.findInfoById(1L)).thenReturn(criarCaixa());

        mockMvc.perform(get("/api/cash/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.openingAmount").value(100.00))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(cashDailyService).findInfoById(1L);
    }

    @Test
    void deveRetornarErroQuandoBuscarCaixaInexistente() throws Exception {
        when(cashDailyService.findInfoById(99L))
                .thenThrow(new ResourceNotFoundException("Não foi possivel encontrar um caixa com esse id"));

        mockMvc.perform(get("/api/cash/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(cashDailyService).findInfoById(99L);
    }
}