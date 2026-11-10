package com.new_big.web.service;

import com.new_big.web.controller.employee.dto.EmployeeRequest;
import com.new_big.web.entity.Employee;
import com.new_big.web.enums.EmployeeRole;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    @DisplayName("Deve salvar um funcionário com sucesso quando CPF e e-mail não estiverem cadastrados")
    void deveSalvarFuncionarioComSucesso() {
        // Arrange (Dado)
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Ana Souza");
        request.setCpf("12345678901");
        request.setEmail("ana@email.com");
        request.setActive(true);

        when(repository.existsByCpf(anyString())).thenReturn(false);
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (Quando)
        Employee result = employeeService.save(request);

        // Assert (Então)
        assertNotNull(result);
        assertEquals("Ana Souza", result.getName());
        assertEquals("12345678901", result.getCpf());
        verify(repository, times(1)).existsByCpf("12345678901");
        verify(repository, times(1)).existsByEmail("ana@email.com");
        verify(repository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Deve lançar ConflictException ao tentar salvar funcionário com CPF já existente (Caminho Triste)")
    void naoDeveSalvarComCpfDuplicado() {
        // Arrange (Dado)
        EmployeeRequest request = new EmployeeRequest();
        request.setCpf("12345678901");

        when(repository.existsByCpf("12345678901")).thenReturn(true);

        // Act & Assert (Quando & Então)
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            employeeService.save(request);
        });

        assertEquals("Já existe um cliente cadastrado com este CPF", exception.getMessage());
        verify(repository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Deve lançar ConflictException ao tentar salvar funcionário com e-mail já existente (Caminho Triste)")
    void naoDeveSalvarComEmailDuplicado() {
        // Arrange (Dado)
        EmployeeRequest request = new EmployeeRequest();
        request.setCpf("12345678901");
        request.setEmail("ana@email.com");

        when(repository.existsByCpf(anyString())).thenReturn(false);
        when(repository.existsByEmail("ana@email.com")).thenReturn(true);

        // Act & Assert (Quando & Então)
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            employeeService.save(request);
        });

        assertEquals("Já existe um cliente cadastrado com este e-mail", exception.getMessage());
        verify(repository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Deve retornar funcionário ao buscar por ID existente")
    void deveBuscarPorIdComSucesso() {
        // Arrange (Dado)
        Long id = 1L;
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName("Carlos");

        when(repository.findById(id)).thenReturn(Optional.of(employee));

        // Act (Quando)
        Employee result = employeeService.findById(id);

        // Assert (Então)
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar por ID inexistente (Caminho Triste)")
    void naoDeveEncontrarPorIdInexistente() {
        // Arrange (Dado)
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert (Quando & Então)
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.findById(id);
        });

        assertTrue(exception.getMessage().contains("Funcionário não encontrado com o ID"));
    }

    @Test
    @DisplayName("Deve alternar o status ativo/inativo do funcionário com sucesso (Toggle)")
    void deveAlternarStatusFuncionario() {
        // Arrange (Dado)
        Long id = 1L;
        Employee employee = new Employee();
        employee.setId(id);
        employee.setActive(true);

        when(repository.findById(id)).thenReturn(Optional.of(employee));
        when(repository.save(any(Employee.class))).thenReturn(employee);

        // Act (Quando)
        employeeService.toggle(id);

        // Assert (Então)
        assertFalse(employee.getActive());
        verify(repository, times(1)).save(employee);
    }

    @Test
    @DisplayName("Deve listar funcionários filtrando por status ativo com sucesso")
    void deveListarPorStatusAtivo() {
        // Arrange (Dado)
        Employee employee = new Employee();
        employee.setActive(true);

        when(repository.findByActive(true)).thenReturn(List.of(employee));

        // Act (Quando)
        List<Employee> result = employeeService.findByActive(true);

        // Assert (Então)
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getActive());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando listagem por status vier vazia (Caminho Triste)")
    void naoDeveEncontrarListaPorStatusVazia() {
        // Arrange (Dado)
        when(repository.findByActive(false)).thenReturn(List.of());

        // Act & Assert (Quando & Então)
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.findByActive(false);
        });

        assertTrue(exception.getMessage().contains("Nenhum funcionário encontrado com active"));
    }
}