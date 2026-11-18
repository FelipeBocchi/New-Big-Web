package com.newbig.web.controller;

import com.newbig.web.controller.salecontroller.SaleController;
import com.newbig.web.controller.salecontroller.dto.SaleAddRequest;
import com.newbig.web.controller.salecontroller.dto.SaleCloseRequest;
import com.newbig.web.controller.salecontroller.dto.SaleOpenRequest;
import com.newbig.web.entity.Customer;
import com.newbig.web.entity.Employee;
import com.newbig.web.entity.Sale;
import com.newbig.web.enums.PaymentMethod;
import com.newbig.web.enums.SaleStatus;
import com.newbig.web.exception.GlobalExceptionHandler;
import com.newbig.web.service.SaleService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SaleController.class)
@Import(GlobalExceptionHandler.class)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SaleService saleService;

    private Sale criarSaleModel() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Carlos Vendedor");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("João Cliente");

        Sale sale = new Sale();
        sale.setId(1L);
        sale.setSubtotal(BigDecimal.valueOf(100.00));
        sale.setDiscount(BigDecimal.valueOf(10.00));
        sale.setTotal(BigDecimal.valueOf(90.00));
        sale.setStatus(SaleStatus.OPENED);
        sale.setCustomer(customer);
        sale.setEmployee(employee);

        return sale;
    }

    private String jsonSaleOpenRequest() {
        return """
                {
                    "customerId": 1,
                    "employeeId": 1,
                    "cashId": 1
                }
                """;
    }

    private String jsonSaleCloseRequest() {
        return """
                {
                    "cashId": 1,
                    "valueArrived": 90.00,
                    "discount": 10.00,
                    "paymentMethod": "PIX"
                }
                """;
    }

    private String jsonSaleAddRequest() {
        return """
                {
                    "id": 1,
                    "cashId": 1,
                    "barcode": "7891010101010",
                    "discountUnit": 0.00,
                    "quantity": 2
                }
                """;
    }

    @Test
    void deveAbrirVendaComSucesso() throws Exception {
        when(saleService.open(any(SaleOpenRequest.class))).thenReturn(criarSaleModel());

        mockMvc.perform(post("/api/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSaleOpenRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPENED"))
                .andExpect(jsonPath("$.customerName").value("João Cliente"))
                .andExpect(jsonPath("$.employeeName").value("Carlos Vendedor"));

        verify(saleService).open(any(SaleOpenRequest.class));
    }

    @Test
    void deveRetornarErroAoAbrirVendaComDadosInvalidos() throws Exception {
        String jsonInvalido = """
                {
                    "customerId": 1,
                    "employeeId": null,
                    "cashId": null
                }
                """;

        mockMvc.perform(post("/api/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(saleService);
    }

    @Test
    void deveFecharVendaComSucesso() throws Exception {
        Sale saleFechada = criarSaleModel();
        saleFechada.setStatus(SaleStatus.COMPLETED);
        // Assumindo que PaymentMethod seja um Enum existente, como PIX
        saleFechada.setPaymentMethod(PaymentMethod.valueOf("PIX"));

        when(saleService.close(any(SaleCloseRequest.class), eq(1L))).thenReturn(saleFechada);

        mockMvc.perform(patch("/api/sale/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSaleCloseRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paymentMethod").value("PIX"))
                .andExpect(jsonPath("$.total").value(90.00));

        verify(saleService).close(any(SaleCloseRequest.class), eq(1L));
    }

    @Test
    void deveRetornarErroAoFecharVendaComDadosInvalidos() throws Exception {
        String jsonInvalido = """
                {
                    "cashId": null,
                    "valueArrived": -10.00,
                    "discount": 0.00,
                    "paymentMethod": null
                }
                """;

        mockMvc.perform(patch("/api/sale/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(saleService);
    }

    @Test
    void deveListarTodasAsVendas() throws Exception {
        when(saleService.findAll()).thenReturn(List.of(criarSaleModel()));

        mockMvc.perform(get("/api/sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerName").value("João Cliente"))
                .andExpect(jsonPath("$[0].employeeName").value("Carlos Vendedor"));

        verify(saleService).findAll();
    }

    @Test
    void deveAdicionarItensNaVendaComSucesso() throws Exception {
        String mensagemEsperada = "Item adicionado com sucesso!";
        when(saleService.addItems(any(SaleAddRequest.class))).thenReturn(mensagemEsperada);

        mockMvc.perform(put("/api/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSaleAddRequest()))
                .andExpect(status().isOk())
                .andExpect(content().string(mensagemEsperada));

        verify(saleService).addItems(any(SaleAddRequest.class));
    }

    @Test
    void deveRetornarErroAoAdicionarItensComDadosInvalidos() throws Exception {
        String jsonInvalido = """
                {
                    "id": null,
                    "cashId": null,
                    "barcode": "",
                    "quantity": 0
                }
                """;

        mockMvc.perform(put("/api/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(saleService);
    }
}