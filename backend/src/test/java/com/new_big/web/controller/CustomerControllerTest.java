package com.new_big.web.controller;

import com.new_big.web.controller.customer.CustomerController;
import com.new_big.web.controller.customer.dto.CustomerRequest;
import com.new_big.web.entity.Customer;
import com.new_big.web.service.CustomerService;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.exception.GlobalExceptionHandler;

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

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    private Customer criarCliente() {
        Customer cliente = new Customer();
        cliente.setId(1L);
        cliente.setName("João");
        cliente.setCpf("12345678901");
        cliente.setPhone("45999999999");
        cliente.setEmail("joao@email.com");
        cliente.setActive(true);
        cliente.setBirthDate(LocalDate.of(2000, 1, 1));
        return cliente;
    }

    private String jsonCliente() {
        return """
                {
                    "name": "João",
                    "cpf": "12345678901",
                    "phone": "45999999999",
                    "email": "joao@email.com",
                    "active": true,
                    "birthDate": "2000-01-01"
                }
                """;
    }

    @Test
    void deveCadastrarCliente() throws Exception {
        when(customerService.save(any(CustomerRequest.class)))
                .thenReturn(criarCliente());

        mockMvc.perform(post("/api/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));

        verify(customerService).save(any(CustomerRequest.class));
    }

    @Test
    void deveRetornarClientePorId() throws Exception {
        when(customerService.findById(1L)).thenReturn(criarCliente());

        mockMvc.perform(get("/api/customer/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João"));

        verify(customerService).findById(1L);
    }

    @Test
    void deveRetornarErroQuandoClienteNaoExistir() throws Exception {
        when(customerService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Cliente não encontrado"));

        mockMvc.perform(get("/api/customer/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(customerService).findById(99L);
    }

    @Test
    void deveListarClientes() throws Exception {
        when(customerService.findAll()).thenReturn(List.of(criarCliente()));

        mockMvc.perform(get("/api/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("João"));

        verify(customerService).findAll();
    }

    @Test
    void deveAtualizarCliente() throws Exception {
        when(customerService.update(eq(1L), any(CustomerRequest.class)))
                .thenReturn(criarCliente());

        mockMvc.perform(put("/api/customer/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João"));

        verify(customerService).update(eq(1L), any(CustomerRequest.class));
    }

    @Test
    void deveInativarCliente() throws Exception {
        doNothing().when(customerService).inactivate(1L);

        mockMvc.perform(delete("/api/customer/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(customerService).inactivate(1L);
    }

    @Test
    void deveListarClientesPorStatus() throws Exception {
        when(customerService.findByActive(true))
                .thenReturn(List.of(criarCliente()));

        mockMvc.perform(get("/api/customer/active/{active}", true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));

        verify(customerService).findByActive(true);
    }

    @Test
    void deveRetornarErroQuandoNaoHouverClientesComStatus() throws Exception {
        when(customerService.findByActive(false))
                .thenThrow(new ResourceNotFoundException("Nenhum cliente encontrado"));

        mockMvc.perform(get("/api/customer/active/{active}", false))
                .andExpect(status().isNotFound());

        verify(customerService).findByActive(false);
    }

    @Test
    void deveRetornarErroQuandoDadosForemInvalidos() throws Exception {
        String jsonInvalido = """
                {
                    "name": "",
                    "cpf": "",
                    "phone": "123",
                    "email": "email-invalido",
                    "active": null,
                    "birthDate": "2999-01-01"
                }
                """;

        mockMvc.perform(post("/api/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customerService);
    }

    @Test
    void deveRetornarConflitoAoCadastrarClienteDuplicado() throws Exception {
        when(customerService.save(any(CustomerRequest.class)))
                .thenThrow(new ConflictException("CPF já cadastrado"));

        mockMvc.perform(post("/api/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCliente()))
                .andExpect(status().isConflict());

        verify(customerService).save(any(CustomerRequest.class));
    }
}