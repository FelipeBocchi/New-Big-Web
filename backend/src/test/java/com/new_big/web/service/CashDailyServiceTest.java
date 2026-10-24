package com.new_big.web.service;

import com.new_big.web.controller.cashDailyController.dto.CashDailyClosedRequest;
import com.new_big.web.controller.cashDailyController.dto.CashDailyOpenedRequest;
import com.new_big.web.entity.CashDailyLog;
import com.new_big.web.entity.Employee;
import com.new_big.web.enums.CashStatus;
import com.new_big.web.enums.PaymentMethod;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.CashDailyRepository;
import com.new_big.web.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CashDailyServiceTest {

    private static final Long CASH_ID = 1L;
    private static final Long EMPLOYEE_ID = 10L;

    private static final BigDecimal OPENING_AMOUNT = new BigDecimal("100.00");

    @Mock
    private CashDailyRepository repository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CashDailyService service;

    private Employee employee;

    @BeforeEach
    void setup() {
        employee = new Employee();
    }

    //  Testes na função Open

    // GIVE:
    // WHEN:
    // Then:
    @Test
    void caso_Abrir_Caixa_Quando_Funcionario_Existir() {

        // Give
        CashDailyOpenedRequest request = createOpenedRequest(OPENING_AMOUNT, EMPLOYEE_ID);

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(employee));

        when(repository.save(any(CashDailyLog.class)))
                .thenAnswer(invocation -> invocation.<CashDailyLog>getArgument(0)
                );

        CashDailyLog result = service.open(request);

        assertAll(
                () -> assertMoneyEquals(
                        OPENING_AMOUNT,
                        result.getOpeningAmount()
                ),
                () -> assertMoneyEquals(
                        BigDecimal.ZERO,
                        result.getTotal()
                ),
                () -> assertMoneyEquals(
                        OPENING_AMOUNT,
                        result.getExpectedClosingAmount()
                ),
                () -> assertNotNull(result.getOpenedAt()),
                () -> assertNull(result.getClosingAmount()),
                () -> assertNull(result.getClosedAt()),
                () -> assertSame(employee, result.getOpenedBy()),
                () -> assertNull(result.getClosedBy()),
                () -> assertEquals(CashStatus.OPEN, result.getStatus()),
                () -> assertNotNull(
                        result.getSales(),
                        "A coleção de vendas deve começar vazia, não nula."
                )
        );

        verify(employeeRepository).findById(EMPLOYEE_ID);
        verify(repository).save(result);
    }

    @Test
    void deveLancarExcecaoAoAbrirCaixaQuandoFuncionarioNaoExistir() {

        // Arrange
        CashDailyOpenedRequest request = createOpenedRequest(
                OPENING_AMOUNT,
                EMPLOYEE_ID
        );

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.open(request)
        );

        // Assert
        assertEquals(
                "Funcionário não encontrado com o id",
                exception.getMessage()
        );

        verify(employeeRepository).findById(EMPLOYEE_ID);
        verify(repository, never()).save(any(CashDailyLog.class));
    }

    // =========================
    // ADD AMOUNT TO CASH
    // =========================

    @Test
    void deveAtualizarTotalEValorEsperadoQuandoPagamentoForDinheiro() {

        // Arrange
        CashDailyLog cash = createCash(
                new BigDecimal("200.00"),
                new BigDecimal("100.00")
        );

        BigDecimal saleAmount = new BigDecimal("20.00");

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        // Act
        service.addAmountToCash(
                CASH_ID,
                saleAmount,
                PaymentMethod.CASH
        );

        // Assert
        assertAll(
                () -> assertMoneyEquals(
                        new BigDecimal("220.00"),
                        cash.getTotal()
                ),
                () -> assertMoneyEquals(
                        new BigDecimal("120.00"),
                        cash.getExpectedClosingAmount()
                )
        );

        verify(repository).findById(CASH_ID);
        verify(repository).save(cash);
    }

    @Test
    void deveAtualizarApenasTotalQuandoPagamentoNaoForDinheiro() {

        // Arrange
        CashDailyLog cash = createCash(
                new BigDecimal("200.00"),
                new BigDecimal("100.00")
        );

        BigDecimal saleAmount = new BigDecimal("20.00");

        PaymentMethod nonCashMethod = Arrays.stream(
                        PaymentMethod.values()
                )
                .filter(method -> method != PaymentMethod.CASH)
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "É necessário existir um método de pagamento diferente de CASH."
                        )
                );

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        // Act
        service.addAmountToCash(
                CASH_ID,
                saleAmount,
                nonCashMethod
        );

        // Assert
        assertAll(
                () -> assertMoneyEquals(
                        new BigDecimal("220.00"),
                        cash.getTotal()
                ),
                () -> assertMoneyEquals(
                        new BigDecimal("100.00"),
                        cash.getExpectedClosingAmount()
                )
        );

        verify(repository).findById(CASH_ID);
        verify(repository).save(cash);
    }

    @Test
    void deveLancarExcecaoAoAdicionarValorQuandoCaixaNaoExistir() {

        // Arrange
        BigDecimal saleAmount = new BigDecimal("20.00");

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.addAmountToCash(
                        CASH_ID,
                        saleAmount,
                        PaymentMethod.CASH
                )
        );

        // Assert
        assertEquals(
                "Não foi possivel encontrar um caixa com esse id",
                exception.getMessage()
        );

        verify(repository).findById(CASH_ID);
        verify(repository, never()).save(any(CashDailyLog.class));
    }

    // =========================
    // CLOSE
    // =========================

    @Test
    void deveFecharCaixaQuandoValorEFuncionarioForemValidos() {

        // Arrange
        CashDailyLog cash = createCash(
                new BigDecimal("200.00"),
                OPENING_AMOUNT
        );

        CashDailyClosedRequest request = createClosedRequest(
                OPENING_AMOUNT,
                EMPLOYEE_ID
        );

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(employee));

        when(repository.save(any(CashDailyLog.class)))
                .thenAnswer(invocation ->
                        invocation.<CashDailyLog>getArgument(0)
                );

        // Act
        CashDailyLog result = service.close(request, CASH_ID);

        // Assert
        assertAll(
                () -> assertSame(cash, result),
                () -> assertMoneyEquals(
                        OPENING_AMOUNT,
                        result.getClosingAmount()
                ),
                () -> assertNotNull(result.getClosedAt()),
                () -> assertSame(employee, result.getClosedBy()),
                () -> assertEquals(CashStatus.CLOSED, result.getStatus())
        );

        verify(repository).findById(CASH_ID);
        verify(employeeRepository).findById(EMPLOYEE_ID);
        verify(repository).save(cash);
    }

    @Test
    void deveLancarConflitoQuandoValorDeFechamentoForDiferente() {

        // Arrange
        CashDailyLog cash = createCash(
                new BigDecimal("200.00"),
                new BigDecimal("100.00")
        );

        CashDailyClosedRequest request = createClosedRequest(
                new BigDecimal("90.00"),
                EMPLOYEE_ID
        );

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        // Act
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.close(request, CASH_ID)
        );

        // Assert
        assertEquals(
                "Valor experado no caixa em dinheiro não é compativel com o valor informado",
                exception.getMessage()
        );

        verify(repository).findById(CASH_ID);
        verify(employeeRepository, never()).findById(any());
        verify(repository, never()).save(any(CashDailyLog.class));
    }

    @Test
    void deveLancarExcecaoAoFecharCaixaQuandoCaixaNaoExistir() {

        // Arrange
        CashDailyClosedRequest request = createClosedRequest(
                OPENING_AMOUNT,
                EMPLOYEE_ID
        );

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.close(request, CASH_ID)
        );

        // Assert
        assertEquals(
                "Caixa não encontrado no banco com esse id",
                exception.getMessage()
        );

        verify(repository).findById(CASH_ID);
        verifyNoInteractions(employeeRepository);
        verify(repository, never()).save(any(CashDailyLog.class));
    }

    @Test
    void deveLancarExcecaoAoFecharCaixaQuandoFuncionarioNaoExistir() {

        // Arrange
        CashDailyLog cash = createCash(
                new BigDecimal("200.00"),
                OPENING_AMOUNT
        );

        CashDailyClosedRequest request = createClosedRequest(
                OPENING_AMOUNT,
                EMPLOYEE_ID
        );

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.close(request, CASH_ID)
        );

        // Assert
        assertEquals(
                "Funcionário não encontrado com ess id",
                exception.getMessage()
        );

        verify(repository).findById(CASH_ID);
        verify(employeeRepository).findById(EMPLOYEE_ID);
        verify(repository, never()).save(any(CashDailyLog.class));
    }

    @Test
    void deveAceitarFechamentoComValoresMonetariosNumericamenteIguais() {

        // Arrange
        CashDailyLog cash = createCash(
                new BigDecimal("200.00"),
                new BigDecimal("100.00")
        );

        CashDailyClosedRequest request = createClosedRequest(
                new BigDecimal("100.0"),
                EMPLOYEE_ID
        );

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(employee));

        when(repository.save(any(CashDailyLog.class)))
                .thenAnswer(invocation ->
                        invocation.<CashDailyLog>getArgument(0)
                );

        // Act
        CashDailyLog result = service.close(request, CASH_ID);

        // Assert
        assertMoneyEquals(
                new BigDecimal("100.00"),
                result.getClosingAmount()
        );

        assertEquals(CashStatus.CLOSED, result.getStatus());

        verify(repository).save(cash);
    }

    // =========================
    // FIND ALL
    // =========================

    @Test
    void deveRetornarTodosOsCaixasEncontrados() {

        // Arrange
        CashDailyLog firstCash = createCash(
                new BigDecimal("150.00"),
                new BigDecimal("100.00")
        );

        CashDailyLog secondCash = createCash(
                new BigDecimal("250.00"),
                new BigDecimal("200.00")
        );

        List<CashDailyLog> expected = List.of(
                firstCash,
                secondCash
        );

        when(repository.findAll()).thenReturn(expected);

        // Act
        List<CashDailyLog> result = service.findAll();

        // Assert
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertSame(firstCash, result.get(0)),
                () -> assertSame(secondCash, result.get(1))
        );

        verify(repository).findAll();
    }

    // =========================
    // FIND INFO BY ID
    // =========================

    @Test
    void deveRetornarCaixaQuandoIdExistir() {

        // Arrange
        CashDailyLog cash = createCash(
                new BigDecimal("200.00"),
                OPENING_AMOUNT
        );

        when(repository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        // Act
        CashDailyLog result = service.findInfoById(CASH_ID);

        // Assert
        assertSame(cash, result);

        verify(repository).findById(CASH_ID);
    }

    @Test
    void deveLancarExcecaoQuandoIdDoCaixaNaoExistir() {

        // Arrange
        when(repository.findById(CASH_ID))
                .thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findInfoById(CASH_ID)
        );

        // Assert
        assertEquals(
                "Não foi possivel encontrar um caixa com esse id",
                exception.getMessage()
        );

        verify(repository).findById(CASH_ID);
    }

    // =========================
    // HELPERS
    // =========================

    private CashDailyOpenedRequest createOpenedRequest(BigDecimal openingAmount, Long employeeId
    ) {
        CashDailyOpenedRequest request = new CashDailyOpenedRequest();

        request.setOpeningAmount(openingAmount);
        request.setEmployeeId(employeeId);

        return request;
    }

    private CashDailyClosedRequest createClosedRequest(BigDecimal closingAmount, Long employeeId
    ) {
        CashDailyClosedRequest request = new CashDailyClosedRequest();

        request.setClosingAmount(closingAmount);
        request.setEmployeeId(employeeId);

        return request;
    }

    private CashDailyLog createCash(BigDecimal total, BigDecimal expectedClosingAmount
    ) {
        CashDailyLog cash = new CashDailyLog();

        cash.setId(CASH_ID);
        cash.setOpeningAmount(OPENING_AMOUNT);
        cash.setTotal(total);
        cash.setExpectedClosingAmount(expectedClosingAmount);
        cash.setOpenedAt(
                LocalDateTime.of(2026, 10, 20, 9, 0)
        );
        cash.setStatus(CashStatus.OPEN);
        cash.setSales(new ArrayList<>());

        return cash;
    }

    private static void assertMoneyEquals(BigDecimal expected, BigDecimal actual
    ) {
        assertNotNull(actual, "O valor monetário não deveria ser nulo.");

        assertEquals(
                0,
                expected.compareTo(actual),
                () -> "Esperado: " + expected + ", recebido: " + actual
        );
    }

}
