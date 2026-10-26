package com.new_big.web.service;

import com.new_big.web.controller.customer.dto.CustomerRequest;
import com.new_big.web.entity.Customer;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.CustomerRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerService service;

    private Customer customer;
    private CustomerRequest request;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("João");
        customer.setCpf("12345678901");
        customer.setPhone("45999999999");
        customer.setEmail("joao@email.com");
        customer.setActive(true);
        customer.setBirthDate(LocalDate.of(2000, 1, 1));

        request = new CustomerRequest();
        request.setName("João");
        request.setCpf("12345678901");
        request.setPhone("45999999999");
        request.setEmail("joao@email.com");
        request.setActive(true);
        request.setBirthDate(LocalDate.of(2000, 1, 1));
    }

    // SAVE

    @Test
    void deveSalvarCliente() {
        when(repository.existsByCpf(request.getCpf())).thenReturn(false);
        when(repository.existsByEmail(request.getEmail())).thenReturn(false);
        when(repository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer salvo = invocation.getArgument(0);
            salvo.setId(1L);
            return salvo;
        });

        Customer resultado = service.save(request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("João", resultado.getName());
        assertEquals("12345678901", resultado.getCpf());
        assertEquals("joao@email.com", resultado.getEmail());

        verify(repository).existsByCpf(request.getCpf());
        verify(repository).existsByEmail(request.getEmail());
        verify(repository).save(any(Customer.class));
    }

    @Test
    void deveLancarExcecaoAoSalvarCpfDuplicado() {
        when(repository.existsByCpf(request.getCpf())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.save(request));

        verify(repository, never()).save(any(Customer.class));
        verify(repository, never()).existsByEmail(anyString());
    }

    @Test
    void deveLancarExcecaoAoSalvarEmailDuplicado() {
        when(repository.existsByCpf(request.getCpf())).thenReturn(false);
        when(repository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.save(request));

        verify(repository, never()).save(any(Customer.class));
    }

    // FIND BY ID

    @Test
    void deveBuscarClientePorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        Customer resultado = service.findById(1L);

        assertEquals(customer, resultado);
        assertEquals("João", resultado.getName());
        verify(repository).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoExistePorId() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));
    }

    // FIND ALL

    @Test
    void deveListarTodosOsClientes() {
        when(repository.findAll()).thenReturn(List.of(customer));

        List<Customer> resultado = service.findAll();

        assertEquals(1, resultado.size());
        assertEquals(customer, resultado.get(0));
        verify(repository).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemClientes() {
        when(repository.findAll()).thenReturn(List.of());

        List<Customer> resultado = service.findAll();

        assertTrue(resultado.isEmpty());
    }

    // UPDATE

    @Test
    void deveAtualizarClienteSemAlterarCpfEmail() {
        request.setName("João Atualizado");

        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.save(any(Customer.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        Customer resultado = service.update(1L, request);

        assertEquals("João Atualizado", resultado.getName());
        verify(repository).save(customer);
        verify(repository, never()).existsByCpf(anyString());
        verify(repository, never()).existsByEmail(anyString());
    }

    @Test
    void deveAtualizarClienteComNovoCpfEEmailDisponiveis() {
        request.setCpf("98765432100");
        request.setEmail("novo@email.com");

        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.existsByCpf(request.getCpf())).thenReturn(false);
        when(repository.existsByEmail(request.getEmail())).thenReturn(false);
        when(repository.save(any(Customer.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        Customer resultado = service.update(1L, request);

        assertEquals("98765432100", resultado.getCpf());
        assertEquals("novo@email.com", resultado.getEmail());
        verify(repository).save(customer);
    }

    @Test
    void deveLancarExcecaoAoAtualizarParaCpfDuplicado() {
        request.setCpf("98765432100");

        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.existsByCpf(request.getCpf())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.update(1L, request));

        verify(repository, never()).save(any(Customer.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarParaEmailDuplicado() {
        request.setEmail("outro@email.com");

        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.update(1L, request));

        verify(repository, never()).save(any(Customer.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarClienteInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(1L, request));

        verify(repository, never()).save(any(Customer.class));
    }

    // INACTIVATE / ACTIVATE

    @Test
    void deveInativarClienteAtivo() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.save(any(Customer.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        service.inactivate(1L);

        assertFalse(customer.getActive());
        verify(repository).save(customer);
    }

    @Test
    void deveAtivarClienteInativo() {
        customer.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        when(repository.save(any(Customer.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        service.inactivate(1L);

        assertTrue(customer.getActive());
        verify(repository).save(customer);
    }

    @Test
    void deveLancarExcecaoAoAlterarStatusDeClienteInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.inactivate(1L));

        verify(repository, never()).save(any(Customer.class));
    }

    // FIND BY ACTIVE

    @Test
    void deveBuscarClientesAtivos() {
        when(repository.findByActive(true)).thenReturn(List.of(customer));

        List<Customer> resultado = service.findByActive(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActive());
        verify(repository).findByActive(true);
    }

    @Test
    void deveBuscarClientesInativos() {
        customer.setActive(false);
        when(repository.findByActive(false)).thenReturn(List.of(customer));

        List<Customer> resultado = service.findByActive(false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).getActive());
    }

    @Test
    void deveLancarExcecaoQuandoNaoExistemClientesComStatusInformado() {
        when(repository.findByActive(false)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findByActive(false));
    }
}