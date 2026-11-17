package com.newbig.web.service;

import com.newbig.web.controller.employee.dto.EmployeeRequest;
import com.newbig.web.entity.Employee;
import com.newbig.web.enums.EmployeeRole;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService — testes unitários")
class EmployeeServiceTest {
    private static final Long ID = 1L;

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @Nested
    @DisplayName("save — cadastro")
    class Save {
        @Test
        @DisplayName("Deve mapear os dados e retornar o funcionário salvo")
        void deveSalvar() {
            // Arrange
            EmployeeRequest request = request();
            Employee saved = new Employee();
            saved.setId(ID);

            when(repository.existsByCpf(request.getCpf())).thenReturn(false);
            when(repository.existsByEmail(request.getEmail())).thenReturn(false);
            when(repository.save(any(Employee.class))).thenReturn(saved);

            // Act
            Employee result = service.save(request);

            // Assert
            ArgumentCaptor<Employee> captor =
                    ArgumentCaptor.forClass(Employee.class);

            verify(repository).save(captor.capture());
            assertMapped(request, captor.getValue());
            assertSame(saved, result);
        }

        @ParameterizedTest(name = "Campo duplicado: {0}")
        @ValueSource(strings = {"CPF", "EMAIL"})
        @DisplayName("Deve rejeitar identificador duplicado sem salvar")
        void deveRejeitarDuplicidade(String field) {
            // Arrange
            EmployeeRequest request = request();
            stubDuplicate(request, field);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.save(request)
            );

            verify(repository, never()).save(any(Employee.class));
        }
    }

    @Nested
    @DisplayName("Consultas")
    class Queries {
        @Test
        @DisplayName("Deve retornar funcionário pelo ID")
        void deveBuscarPorId() {
            // Arrange
            Employee employee = employee();
            when(repository.findById(ID))
                    .thenReturn(Optional.of(employee));

            // Act
            Employee result = service.findById(ID);

            // Assert
            assertSame(employee, result);
        }

        @Test
        @DisplayName("Deve rejeitar ID inexistente")
        void deveRejeitarIdAusente() {
            // Arrange
            when(repository.findById(ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.findById(ID)
            );
        }

        @ParameterizedTest(name = "Lista vazia: {0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("Deve retornar a listagem, inclusive vazia")
        void deveListar(boolean empty) {
            // Arrange
            List<Employee> expected =
                    empty ? List.of() : List.of(employee());

            when(repository.findAll()).thenReturn(expected);

            // Act
            List<Employee> result = service.findAll();

            // Assert
            assertEquals(expected, result);
        }

        @ParameterizedTest(name = "Ativo: {0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("Deve encaminhar o filtro de atividade")
        void deveFiltrarAtividade(boolean active) {
            // Arrange
            Employee employee = employee();
            employee.setActive(active);

            when(repository.findByActive(active))
                    .thenReturn(List.of(employee));

            // Act
            List<Employee> result = service.findByActive(active);

            // Assert
            assertEquals(List.of(employee), result);
        }

        @ParameterizedTest(name = "Ativo: {0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("Deve rejeitar filtro de atividade sem resultados — contrato atual")
        void deveRejeitarFiltroVazio(boolean active) {
            // Arrange
            when(repository.findByActive(active)).thenReturn(List.of());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.findByActive(active)
            );
        }

        @ParameterizedTest(name = "Cargo: {0}; ativo: {1}; lista vazia: {2}")
        @CsvSource({
                "CASHIER,true,false",
                "MANAGER,false,true"
        })
        @DisplayName("Deve encaminhar cargo e atividade e retornar a consulta")
        void deveFiltrarCargo(
                EmployeeRole role,
                boolean active,
                boolean empty
        ) {
            // Arrange
            Employee employee = employee();
            employee.setRole(role);
            employee.setActive(active);

            List<Employee> expected =
                    empty ? List.of() : List.of(employee);

            when(repository.findByRoleAndActive(role, active))
                    .thenReturn(expected);

            // Act
            List<Employee> result =
                    service.findByRoleAndActive(role, active);

            // Assert
            assertEquals(expected, result);
        }
    }

    @Nested
    @DisplayName("update e updatePartial — alterações")
    class Updates {
        @ParameterizedTest(name = "Alterar CPF: {0}; alterar e-mail: {1}")
        @CsvSource({
                "false,false",
                "true,false",
                "false,true",
                "true,true"
        })
        @DisplayName("Deve atualizar dados com identificadores mantidos ou novos e livres")
        void deveAtualizar(boolean changeCpf, boolean changeEmail) {
            // Arrange
            Employee employee = employee();
            EmployeeRequest request = changedRequest();

            if (changeCpf) request.setCpf("98765432100");
            if (changeEmail) request.setEmail("novo@example.com");

            when(repository.findById(ID))
                    .thenReturn(Optional.of(employee));

            if (changeCpf) {
                when(repository.existsByCpf(request.getCpf()))
                        .thenReturn(false);
            }
            if (changeEmail) {
                when(repository.existsByEmail(request.getEmail()))
                        .thenReturn(false);
            }

            when(repository.save(employee)).thenReturn(employee);

            // Act
            Employee result = service.update(request, ID);

            // Assert
            assertSame(employee, result);
            assertMapped(request, employee);
            verify(repository).save(employee);

            if (!changeCpf) {
                verify(repository, never()).existsByCpf(anyString());
            }
            if (!changeEmail) {
                verify(repository, never()).existsByEmail(anyString());
            }
        }

        @ParameterizedTest(name = "PATCH: {0}; campo duplicado: {1}")
        @CsvSource({
                "false,CPF",
                "false,EMAIL",
                "true,CPF",
                "true,EMAIL"
        })
        @DisplayName("Deve rejeitar CPF ou e-mail de outro funcionário")
        void deveRejeitarDuplicidade(boolean partial, String field) {
            // Arrange
            Employee employee = employee();
            EmployeeRequest request = new EmployeeRequest();
            request.setCpf("98765432100");
            request.setEmail("novo@example.com");

            when(repository.findById(ID))
                    .thenReturn(Optional.of(employee));

            stubDuplicate(request, field);

            // Act + Assert
            assertThrows(ConflictException.class, () -> {
                if (partial) service.updatePartial(request, ID);
                else service.update(request, ID);
            });

            verify(repository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("Deve aplicar PATCH incluindo valores false e data de nascimento")
        void deveAplicarPatch() {
            // Arrange
            Employee employee = employee();
            EmployeeRequest request = changedRequest();
            request.setCpf("98765432100");
            request.setEmail("novo@example.com");
            request.setActive(false);
            request.setAdmin(false);

            when(repository.findById(ID))
                    .thenReturn(Optional.of(employee));
            when(repository.existsByCpf(request.getCpf()))
                    .thenReturn(false);
            when(repository.existsByEmail(request.getEmail()))
                    .thenReturn(false);
            when(repository.save(employee)).thenReturn(employee);

            // Act
            Employee result = service.updatePartial(request, ID);

            // Assert
            assertSame(employee, result);
            assertMapped(request, employee);
            assertEquals(request.getBirthDate(), employee.getBirthDate());
            verify(repository).save(employee);
        }

        @ParameterizedTest(name = "Reenviar CPF e e-mail atuais: {0}")
        @ValueSource(booleans = {false, true})
        @DisplayName("Deve preservar campos omitidos e aceitar identificadores atuais no PATCH")
        void devePreservarCampos(boolean sendIdentity) {
            // Arrange
            Employee employee = employee();
            EmployeeRequest expected = request();
            EmployeeRequest patch = new EmployeeRequest();

            if (sendIdentity) {
                patch.setCpf(employee.getCpf());
                patch.setEmail(employee.getEmail());
            }

            when(repository.findById(ID))
                    .thenReturn(Optional.of(employee));
            when(repository.save(employee)).thenReturn(employee);

            // Act
            service.updatePartial(patch, ID);

            // Assert
            assertMapped(expected, employee);
            assertEquals(expected.getBirthDate(), employee.getBirthDate());
            verify(repository, never()).existsByCpf(anyString());
            verify(repository, never()).existsByEmail(anyString());
            verify(repository).save(employee);
        }

        @ParameterizedTest(name = "PATCH: {0}")
        @ValueSource(booleans = {false, true})
        @DisplayName("Deve rejeitar alteração de funcionário inexistente")
        void deveRejeitarAusente(boolean partial) {
            // Arrange
            EmployeeRequest request = request();
            when(repository.findById(ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                if (partial) service.updatePartial(request, ID);
                else service.update(request, ID);
            });

            verify(repository, never()).save(any(Employee.class));
        }
    }

    @Nested
    @DisplayName("toggle — atividade")
    class Toggle {
        @ParameterizedTest(name = "Antes: {0}; depois: {1}")
        @CsvSource({"true,false", "false,true"})
        @DisplayName("Deve alternar a atividade nas duas direções")
        void deveAlternar(boolean before, boolean after) {
            // Arrange
            Employee employee = employee();
            employee.setActive(before);

            when(repository.findById(ID))
                    .thenReturn(Optional.of(employee));

            // Act
            service.toggle(ID);

            // Assert
            assertEquals(after, employee.getActive());
            verify(repository).save(employee);
        }

        @Test
        @DisplayName("Deve rejeitar toggle de funcionário inexistente")
        void deveRejeitarAusente() {
            // Arrange
            when(repository.findById(ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.toggle(ID)
            );

            verify(repository, never()).save(any(Employee.class));
        }
    }

    private EmployeeRequest request() {
        EmployeeRequest r = new EmployeeRequest();
        r.setName("Ana Souza");
        r.setCpf("12345678901");
        r.setPhone("11999999999");
        r.setEmail("ana@example.com");
        r.setUsername("ana");
        r.setPassword("senha-apenas-para-teste");
        r.setActive(true);
        r.setAdmin(true);
        r.setRole(EmployeeRole.CASHIER);
        r.setBirthDate(LocalDate.of(1995, 6, 15));
        return r;
    }

    private EmployeeRequest changedRequest() {
        EmployeeRequest r = request();
        r.setName("Beatriz");
        r.setPhone("11888888888");
        r.setUsername("bia");
        r.setActive(false);
        r.setAdmin(false);
        r.setRole(EmployeeRole.MANAGER);
        r.setBirthDate(LocalDate.of(1990, 1, 10));
        return r;
    }

    private Employee employee() {
        EmployeeRequest r = request();
        Employee e = new Employee();
        e.setId(ID);
        e.setName(r.getName());
        e.setCpf(r.getCpf());
        e.setPhone(r.getPhone());
        e.setEmail(r.getEmail());
        e.setUsername(r.getUsername());
        e.setActive(r.getActive());
        e.setAdmin(r.getAdmin());
        e.setRole(r.getRole());
        e.setBirthDate(r.getBirthDate());
        return e;
    }

    private void stubDuplicate(EmployeeRequest r, String field) {
        when(repository.existsByCpf(r.getCpf()))
                .thenReturn(field.equals("CPF"));

        if (field.equals("EMAIL")) {
            when(repository.existsByEmail(r.getEmail())).thenReturn(true);
        }
    }

    private void assertMapped(EmployeeRequest r, Employee e) {
        // birthDate no save/update e armazenamento de senha:
        // lacunas discutidas separadamente.
        assertAll(
                () -> assertEquals(r.getName(), e.getName()),
                () -> assertEquals(r.getCpf(), e.getCpf()),
                () -> assertEquals(r.getPhone(), e.getPhone()),
                () -> assertEquals(r.getEmail(), e.getEmail()),
                () -> assertEquals(r.getUsername(), e.getUsername()),
                () -> assertEquals(r.getActive(), e.getActive()),
                () -> assertEquals(r.getAdmin(), e.getAdmin()),
                () -> assertEquals(r.getRole(), e.getRole())
        );
    }
}