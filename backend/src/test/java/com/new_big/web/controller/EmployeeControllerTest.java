package com.new_big.web.controller.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.new_big.web.controller.employee.dto.EmployeeRequest;
import com.new_big.web.entity.Employee;
import com.new_big.web.enums.EmployeeRole;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService service;

    @Test
    @DisplayName("Deve criar um funcionário com sucesso e retornar status 201 Created")
    void deveCriarFuncionarioComSucesso() throws Exception {
        // Arrange (Dado)
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Carlos Silva");
        request.setCpf("12345678901");
        request.setEmail("carlos@email.com");
        request.setUsername("carlos.silva");
        request.setPassword("123456");
        request.setActive(true);
        request.setAdmin(false);
        request.setBirthDate(LocalDate.of(1995, 5, 10));
        request.setRole(EmployeeRole.CASHIER);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName(request.getName());
        employee.setCpf(request.getCpf());
        employee.setEmail(request.getEmail());
        employee.setUsername(request.getUsername());
        employee.setActive(request.getActive());
        employee.setAdmin(request.getAdmin());
        employee.setBirthDate(request.getBirthDate());
        employee.setRole(request.getRole());

        when(service.save(any(EmployeeRequest.class))).thenReturn(employee);

        // Act & Assert (Quando & Então)
        mockMvc.perform(post("/api/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Carlos Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.email").value("carlos@email.com"));
    }

    @Test
    @DisplayName("Deve buscar funcionário por ID com sucesso e retornar status 200 OK")
    void deveBuscarPorIdComSucesso() throws Exception {
        // Arrange (Dado)
        Long id = 1L;
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName("Ana Souza");
        employee.setCpf("98765432100");
        employee.setEmail("ana@email.com");
        employee.setUsername("ana.souza");
        employee.setActive(true);
        employee.setRole(EmployeeRole.MANAGER);

        when(service.findById(id)).thenReturn(employee);

        // Act & Assert (Quando & Então)
        mockMvc.perform(get("/api/employee/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    @DisplayName("Deve listar todos os funcionários com sucesso")
    void deveListarTodosComSucesso() throws Exception {
        // Arrange (Dado)
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Maria");
        employee.setCpf("11122233344");
        employee.setEmail("maria@email.com");
        employee.setUsername("maria");
        employee.setActive(true);

        when(service.findAll()).thenReturn(List.of(employee));

        // Act & Assert (Quando & Então)
        mockMvc.perform(get("/api/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Maria"));
    }

    @Test
    @DisplayName("Deve retornar status 204 No Content ao alternar status (toggle) do funcionário")
    void deveAlternarStatusComSucesso() throws Exception {
        // Arrange (Dado)
        Long id = 1L;

        // Act & Assert (Quando & Então)
        mockMvc.perform(delete("/api/employee/{id}", id))
                .andExpect(status().isNoContent());
    }
}