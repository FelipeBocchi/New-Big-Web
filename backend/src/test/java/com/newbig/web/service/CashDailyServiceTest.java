package com.newbig.web.service;

import com.newbig.web.controller.cashdailycontroller.dto.CashDailyClosedRequest;
import com.newbig.web.controller.cashdailycontroller.dto.CashDailyOpenedRequest;
import com.newbig.web.entity.CashDailyLog;
import com.newbig.web.entity.Employee;
import com.newbig.web.enums.CashStatus;
import com.newbig.web.enums.PaymentMethod;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.CashDailyRepository;
import com.newbig.web.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashDailyService — testes unitários")
class CashDailyServiceTest {

    private static final Long CASH_ID = 1L;
    private static final Long EMPLOYEE_ID = 10L;

    private static final BigDecimal OPENING_AMOUNT =
            new BigDecimal("100.00");

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 12, 10, 0);

    @Mock
    private CashDailyRepository repository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CashDailyService service;

    @Captor
    private ArgumentCaptor<CashDailyLog> cashCaptor;

    @Spy
    private Clock clock = Clock.fixed(
            NOW.toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC
    );

    private MockedStatic<LocalDateTime> dates;

    @BeforeEach
    void fixarData() {
        dates = mockStatic(
                LocalDateTime.class,
                CALLS_REAL_METHODS
        );

        dates.when(LocalDateTime::now).thenReturn(NOW);
    }

    @AfterEach
    void liberarData() {
        dates.close();
    }

    @Nested
    @DisplayName("open — abertura do caixa")
    class Open {

        @Test
        @DisplayName("Deve iniciar caixa aberto com fundo inicial e devolver o retorno do repositório")
        void deveAbrirCaixa() {
            // Arrange
            CashDailyOpenedRequest request = openRequest();
            Employee employee = employee();

            CashDailyLog savedCash = new CashDailyLog();
            savedCash.setId(CASH_ID);

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee));

            when(repository.save(any(CashDailyLog.class)))
                    .thenReturn(savedCash);

            // Act
            CashDailyLog result = service.open(request);

            // Assert
            verify(repository).save(cashCaptor.capture());
            CashDailyLog created = cashCaptor.getValue();

            assertAll(
                    () -> assertSame(savedCash, result),
                    () -> assertMoneyEquals(
                            OPENING_AMOUNT,
                            created.getOpeningAmount()
                    ),
                    () -> assertMoneyEquals(
                            OPENING_AMOUNT,
                            created.getExpectedClosingAmount()
                    ),
                    () -> assertMoneyEquals(
                            BigDecimal.ZERO,
                            created.getTotal()
                    ),
                    () -> assertEquals(NOW, created.getOpenedAt()),
                    () -> assertEquals(CashStatus.OPEN, created.getStatus()),
                    () -> assertSame(employee, created.getOpenedBy()),
                    () -> assertNull(created.getClosingAmount()),
                    () -> assertNull(created.getClosedAt()),
                    () -> assertNull(created.getClosedBy()),
                    () -> assertNotNull(created.getSales()),
                    () -> assertTrue(created.getSales().isEmpty())
            );
        }

        @Test
        @DisplayName("Deve rejeitar abertura quando o funcionário não existir")
        void deveRejeitarFuncionarioAusente() {
            // Arrange
            CashDailyOpenedRequest request = openRequest();

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.open(request)
            );

            verify(repository, never()).save(any(CashDailyLog.class));
        }
    }

    @Nested
    @DisplayName("addAmountToCash — lançamentos financeiros")
    class AddAmount {

        @ParameterizedTest(name = "Pagamento: {0}; dinheiro esperado: {1}")
        @CsvSource({
                "CASH,120.00",
                "CREDIT_CARD,100.00",
                "DEBIT_CARD,100.00",
                "PIX,100.00"
        })
        @DisplayName("Deve atualizar total vendido e alterar dinheiro esperado somente para CASH")
        void deveLancarPagamento(
                PaymentMethod method,
                BigDecimal expectedCash
        ) {
            // Arrange
            CashDailyLog cash = cash();

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            // Act
            service.addAmountToCash(
                    CASH_ID,
                    new BigDecimal("20.00"),
                    method
            );

            // Assert
            assertAll(
                    () -> assertMoneyEquals(
                            new BigDecimal("220.00"),
                            cash.getTotal()
                    ),
                    () -> assertMoneyEquals(
                            expectedCash,
                            cash.getExpectedClosingAmount()
                    ),
                    () -> assertMoneyEquals(
                            OPENING_AMOUNT,
                            cash.getOpeningAmount()
                    ),
                    () -> assertEquals(CashStatus.OPEN, cash.getStatus())
            );

            verify(repository).save(cash);
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("Deve acumular lançamentos em dinheiro e PIX sem substituir saldos anteriores")
        void deveAcumularLancamentos() {
            // Arrange
            CashDailyLog cash = cash();

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            // Act
            service.addAmountToCash(
                    CASH_ID,
                    new BigDecimal("20.00"),
                    PaymentMethod.CASH
            );

            service.addAmountToCash(
                    CASH_ID,
                    new BigDecimal("30.00"),
                    PaymentMethod.PIX
            );

            // Assert
            assertMoneyEquals(
                    new BigDecimal("250.00"),
                    cash.getTotal()
            );

            assertMoneyEquals(
                    new BigDecimal("120.00"),
                    cash.getExpectedClosingAmount()
            );

            verify(repository, times(2)).save(cash);
        }

        @Test
        @DisplayName("Deve rejeitar lançamento em caixa fechado sem alterar valores")
        void deveRejeitarCaixaFechado() {
            // Arrange
            CashDailyLog cash = cash();
            cash.setStatus(CashStatus.CLOSED);

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.addAmountToCash(
                            CASH_ID,
                            new BigDecimal("20.00"),
                            PaymentMethod.CASH
                    )
            );

            assertMoneyEquals(
                    new BigDecimal("200.00"),
                    cash.getTotal()
            );

            assertMoneyEquals(
                    OPENING_AMOUNT,
                    cash.getExpectedClosingAmount()
            );

            verify(repository, never()).save(any(CashDailyLog.class));
        }

        @Test
        @DisplayName("Deve rejeitar lançamento em caixa inexistente")
        void deveRejeitarCaixaAusente() {
            // Arrange
            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.addAmountToCash(
                            CASH_ID,
                            new BigDecimal("20.00"),
                            PaymentMethod.CASH
                    )
            );

            verify(repository, never()).save(any(CashDailyLog.class));
        }
    }

    @Nested
    @DisplayName("close — fechamento do caixa")
    class Close {

        @Test
        @DisplayName("Deve fechar caixa com valor esperado, funcionário e horário de fechamento")
        void deveFecharCaixa() {
            // Arrange
            CashDailyLog cash = cash();
            CashDailyClosedRequest request =
                    closeRequest(OPENING_AMOUNT);
            Employee employee = employee();

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee));

            when(repository.save(cash))
                    .thenAnswer(returnsFirstArg());

            // Act
            CashDailyLog result = service.close(request, CASH_ID);

            // Assert
            assertAll(
                    () -> assertSame(cash, result),
                    () -> assertEquals(CashStatus.CLOSED, result.getStatus()),
                    () -> assertMoneyEquals(
                            OPENING_AMOUNT,
                            result.getClosingAmount()
                    ),
                    () -> assertEquals(NOW, result.getClosedAt()),
                    () -> assertSame(employee, result.getClosedBy()),
                    () -> assertMoneyEquals(
                            new BigDecimal("200.00"),
                            result.getTotal()
                    ),
                    () -> assertMoneyEquals(
                            OPENING_AMOUNT,
                            result.getOpeningAmount()
                    ),
                    () -> assertEquals(
                            NOW.minusHours(1),
                            result.getOpenedAt()
                    )
            );

            verify(repository).save(cash);
        }

        @Test
        @DisplayName("Deve rejeitar fechamento de caixa já fechado")
        void deveRejeitarCaixaFechado() {
            // Arrange
            CashDailyLog cash = cash();
            cash.setStatus(CashStatus.CLOSED);

            CashDailyClosedRequest request =
                    closeRequest(OPENING_AMOUNT);

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.close(request, CASH_ID)
            );

            verify(repository, never()).save(any(CashDailyLog.class));
            verifyNoInteractions(employeeRepository);
        }

        @ParameterizedTest(name = "Valor informado: {0}; esperado: 100.00")
        @ValueSource(strings = {"90.00", "110.00"})
        @DisplayName("Deve rejeitar falta ou sobra de dinheiro — regra atual")
        void deveRejeitarValorDivergente(String value) {
            // Arrange
            CashDailyLog cash = cash();

            CashDailyClosedRequest request =
                    closeRequest(new BigDecimal(value));

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.close(request, CASH_ID)
            );

            assertAll(
                    () -> assertEquals(CashStatus.OPEN, cash.getStatus()),
                    () -> assertNull(cash.getClosingAmount()),
                    () -> assertNull(cash.getClosedAt()),
                    () -> assertNull(cash.getClosedBy())
            );

            verify(repository, never()).save(any(CashDailyLog.class));
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("Deve rejeitar fechamento de caixa inexistente")
        void deveRejeitarCaixaAusente() {
            // Arrange
            CashDailyClosedRequest request =
                    closeRequest(OPENING_AMOUNT);

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.close(request, CASH_ID)
            );

            verify(repository, never()).save(any(CashDailyLog.class));
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("Deve rejeitar fechamento quando o funcionário não existir")
        void deveRejeitarFuncionarioAusente() {
            // Arrange
            CashDailyLog cash = cash();

            CashDailyClosedRequest request =
                    closeRequest(OPENING_AMOUNT);

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.close(request, CASH_ID)
            );

            // Confirma que a rejeição não alterou o caixa
            assertNull(cash.getClosingAmount());
            assertNull(cash.getClosedAt());
            assertNull(cash.getClosedBy());
            assertEquals(CashStatus.OPEN, cash.getStatus());

            verify(repository, never()).save(any(CashDailyLog.class));

            // Mocks não comprovam rollback de banco nem desfazem
            // mudanças feitas em memória antes da exceção.
        }

        @ParameterizedTest(name = "Campo inconsistente: {0}")
        @ValueSource(strings = {
                "STATUS",
                "CLOSING_AMOUNT",
                "CLOSED_AT",
                "CLOSED_BY",
                "TOTAL",
                "EXPECTED"
        })
        @DisplayName("Deve rejeitar operações financeiras em caixa inconsistente")
        void deveRejeitarCaixaInconsistente(String field) {
            // Arrange
            CashDailyLog cash = cash();

            switch (field) {
                case "STATUS" -> cash.setStatus(null);
                case "CLOSING_AMOUNT" -> cash.setClosingAmount(OPENING_AMOUNT);
                case "CLOSED_AT" -> cash.setClosedAt(NOW);
                case "CLOSED_BY" -> cash.setClosedBy(employee());
                case "TOTAL" -> cash.setTotal(null);
                case "EXPECTED" -> cash.setExpectedClosingAmount(null);
            }

            CashStatus previousStatus = cash.getStatus();

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            CashDailyClosedRequest request =
                    closeRequest(OPENING_AMOUNT);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.close(request, CASH_ID)
            );

            assertThrows(
                    ConflictException.class,
                    () -> service.addAmountToCash(
                            CASH_ID,
                            BigDecimal.TEN,
                            PaymentMethod.CASH
                    )
            );

            assertEquals(previousStatus, cash.getStatus());
            verify(repository, never()).save(any(CashDailyLog.class));
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("Deve propagar falha de gravação no fechamento")
        void devePropagarFalhaAoSalvar() {
            // Arrange
            CashDailyLog cash = cash();

            CashDailyClosedRequest request =
                    closeRequest(OPENING_AMOUNT);

            IllegalStateException failure =
                    new IllegalStateException("Falha de gravação");

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee()));

            when(repository.save(cash))
                    .thenThrow(failure);

            // Act + Assert
            IllegalStateException result = assertThrows(
                    IllegalStateException.class,
                    () -> service.close(request, CASH_ID)
            );

            assertSame(failure, result);
            verify(repository).save(cash);
        }
    }

    @Nested
    @DisplayName("Consultas")
    class Queries {

        @ParameterizedTest(name = "Lista vazia: {0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("Deve retornar os caixas encontrados, inclusive lista vazia")
        void deveListar(boolean empty) {
            // Arrange
            List<CashDailyLog> expected =
                    empty ? List.of() : List.of(cash());

            when(repository.findAll()).thenReturn(expected);

            // Act
            List<CashDailyLog> result = service.findAll();

            // Assert
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Deve retornar caixa pelo ID")
        void deveBuscarPorId() {
            // Arrange
            CashDailyLog cash = cash();

            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));

            // Act
            CashDailyLog result = service.findInfoById(CASH_ID);

            // Assert
            assertSame(cash, result);
        }

        @Test
        @DisplayName("Deve rejeitar ID inexistente na consulta")
        void deveRejeitarIdAusente() {
            // Arrange
            when(repository.findById(CASH_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.findInfoById(CASH_ID)
            );
        }
    }

    @ParameterizedTest(name = "Valor inválido: {0}")
    @NullSource
    @ValueSource(strings = {"0.00", "-1.00"})
    @DisplayName("Deve exigir valores positivos na abertura e nos lançamentos")
    void deveRejeitarValoresNaoPositivos(String value) {
        // Arrange
        BigDecimal amount =
                value == null ? null : new BigDecimal(value);

        CashDailyOpenedRequest request = openRequest();
        request.setOpeningAmount(amount);

        // Act + Assert
        assertThrows(
                ConflictException.class,
                () -> service.open(request)
        );

        assertThrows(
                ConflictException.class,
                () -> service.addAmountToCash(
                        CASH_ID,
                        amount,
                        PaymentMethod.CASH
                )
        );

        verifyNoInteractions(repository, employeeRepository);
    }

    @Test
    @DisplayName("Deve rejeitar pagamento sem método informado")
    void deveRejeitarPagamentoNulo() {
        // Arrange — valor válido, método ausente.

        // Act + Assert
        assertThrows(
                ConflictException.class,
                () -> service.addAmountToCash(
                        CASH_ID,
                        BigDecimal.TEN,
                        null
                )
        );

        verifyNoInteractions(repository, employeeRepository);
    }

    @ParameterizedTest(name = "Valor informado: {0}; esperado: 100.00")
    @ValueSource(strings = {"100", "100.0", "100.00"})
    @DisplayName("Deve aceitar valores numericamente iguais com escalas diferentes")
    void deveAceitarEscalasDiferentes(String value) {
        // Arrange
        CashDailyLog cash = cash();

        CashDailyClosedRequest request =
                closeRequest(new BigDecimal(value));

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(employee()));

        when(repository.save(cash))
                .thenReturn(cash);

        // Act
        CashDailyLog result = service.close(request, CASH_ID);

        // Assert
        assertEquals(CashStatus.CLOSED, result.getStatus());
        assertMoneyEquals(OPENING_AMOUNT, result.getClosingAmount());

        verify(repository).save(cash);
    }

    @ParameterizedTest(name = "Fechamento inválido: {0}")
    @NullSource
    @ValueSource(strings = {"-1.00"})
    @DisplayName("Deve rejeitar fechamento sem valor ou com valor negativo")
    void deveRejeitarValorDeFechamentoInvalido(String value) {
        // Arrange
        CashDailyClosedRequest request = closeRequest(
                value == null ? null : new BigDecimal(value)
        );

        // Act + Assert
        assertThrows(
                ConflictException.class,
                () -> service.close(request, CASH_ID)
        );

        verifyNoInteractions(repository, employeeRepository);
    }

    @Test
    @DisplayName("Deve rejeitar requisição de abertura nula")
    void deveRejeitarAberturaNula() {
        // Act + Assert
        assertThrows(
                ConflictException.class,
                () -> service.open(null)
        );

        verifyNoInteractions(repository, employeeRepository);
    }

    @Test
    @DisplayName("Deve rejeitar requisição de fechamento nula")
    void deveRejeitarFechamentoNulo() {
        // Act + Assert
        assertThrows(
                ConflictException.class,
                () -> service.close(null, CASH_ID)
        );

        verifyNoInteractions(repository, employeeRepository);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1})
    @DisplayName("Deve rejeitar IDs inválidos antes de consultar")
    void deveRejeitarIdsInvalidos(Long id) {
        // Arrange
        CashDailyOpenedRequest request = openRequest();
        request.setEmployeeId(id);

        // Act + Assert
        assertThrows(
                ConflictException.class,
                () -> service.findInfoById(id)
        );

        assertThrows(
                ConflictException.class,
                () -> service.open(request)
        );

        verifyNoInteractions(repository, employeeRepository);
    }


    // Helpenes
    private CashDailyOpenedRequest openRequest() {
        CashDailyOpenedRequest request = new CashDailyOpenedRequest();
        request.setEmployeeId(EMPLOYEE_ID);
        request.setOpeningAmount(OPENING_AMOUNT);
        return request;
    }

    private CashDailyClosedRequest closeRequest(BigDecimal amount) {
        CashDailyClosedRequest request = new CashDailyClosedRequest();
        request.setEmployeeId(EMPLOYEE_ID);
        request.setClosingAmount(amount);
        return request;
    }

    private Employee employee() {
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        return employee;
    }

    private CashDailyLog cash() {
        CashDailyLog cash = new CashDailyLog();
        cash.setId(CASH_ID);
        cash.setOpeningAmount(OPENING_AMOUNT);
        cash.setExpectedClosingAmount(OPENING_AMOUNT);
        cash.setTotal(new BigDecimal("200.00"));
        cash.setStatus(CashStatus.OPEN);
        cash.setOpenedAt(NOW.minusHours(1));
        cash.setOpenedBy(employee());
        cash.setSales(new ArrayList<>());
        return cash;
    }

    private static void assertMoneyEquals(
            BigDecimal expected,
            BigDecimal actual
    ) {
        assertNotNull(
                actual,
                "O valor monetário não deveria ser nulo"
        );

        assertEquals(
                0,
                expected.compareTo(actual),
                () -> "Esperado: " + expected + ", recebido: " + actual
        );
    }
}