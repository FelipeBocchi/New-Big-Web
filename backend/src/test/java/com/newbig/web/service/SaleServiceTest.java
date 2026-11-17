package com.newbig.web.service;

import com.newbig.web.controller.batch.dto.BatchConsumption;
import com.newbig.web.controller.salecontroller.dto.SaleAddRequest;
import com.newbig.web.controller.salecontroller.dto.SaleCloseRequest;
import com.newbig.web.controller.salecontroller.dto.SaleOpenRequest;
import com.newbig.web.entity.*;
import com.newbig.web.enums.PaymentMethod;
import com.newbig.web.enums.SaleStatus;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleService — testes unitários")
class SaleServiceTest {

    private static final Long SALE_ID = 1L;
    private static final Long CASH_ID = 2L;
    private static final Long EMPLOYEE_ID = 3L;
    private static final Long CUSTOMER_ID = 4L;
    private static final Long PRODUCT_ID = 5L;
    private static final String BARCODE = "7891234567890";
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal SALE_TOTAL = new BigDecimal("30.00");

    @Mock private SaleRepository repository;
    @Mock private CashDailyRepository cashDailyRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private BatchService batchService;
    @Mock private StockMovementService movementService;
    @Mock private SaleItemService itemService;
    @Mock private CashDailyService cashService;
    @InjectMocks private SaleService service;

    @Nested
    @DisplayName("open — abertura da venda")
    class Open {
        @Test
        @DisplayName("Deve salvar venda aberta, sem cliente e com valores zerados")
        void deveAbrirVendaSemClienteQuandoCaixaEFuncionarioExistirem() {
            // Arrange
            SaleOpenRequest request = createOpenRequest(null);
            CashDailyLog cash = new CashDailyLog();
            Employee employee = new Employee();
            Sale savedSale = new Sale();
            savedSale.setId(SALE_ID);

            when(cashDailyRepository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee));
            when(repository.save(any(Sale.class)))
                    .thenReturn(savedSale);

            // Act
            Sale result = service.open(request);

            // Assert — captura a venda enviada ao repositório
            ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
            verify(repository).save(captor.capture());
            Sale createdSale = captor.getValue();

            assertAll(
                    () -> assertSame(savedSale, result),
                    () -> assertMoneyEquals(ZERO, createdSale.getSubtotal()),
                    () -> assertMoneyEquals(ZERO, createdSale.getDiscount()),
                    () -> assertMoneyEquals(ZERO, createdSale.getTotal()),
                    () -> assertEquals(SaleStatus.OPENED, createdSale.getStatus()),
                    () -> assertNull(createdSale.getPaymentMethod()),
                    () -> assertNull(createdSale.getCustomer()),
                    () -> assertSame(employee, createdSale.getEmployee()),
                    () -> assertSame(cash, createdSale.getCashier())
            );

            verifyNoInteractions(customerRepository);
        }

        @Test
        @DisplayName("Deve vincular o cliente informado à venda")
        void deveAbrirVendaComClienteQuandoClienteExistir() {
            // Arrange
            SaleOpenRequest request = createOpenRequest(CUSTOMER_ID);
            CashDailyLog cash = new CashDailyLog();
            Employee employee = new Employee();
            Customer customer = new Customer();

            when(cashDailyRepository.findById(CASH_ID))
                    .thenReturn(Optional.of(cash));
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee));
            when(customerRepository.findById(CUSTOMER_ID))
                    .thenReturn(Optional.of(customer));
            when(repository.save(any(Sale.class)))
                    .thenAnswer(invocation -> invocation.<Sale>getArgument(0));

            // Act
            Sale result = service.open(request);

            // Assert
            assertAll(
                    () -> assertSame(customer, result.getCustomer()),
                    () -> assertSame(employee, result.getEmployee()),
                    () -> assertSame(cash, result.getCashier()),
                    () -> assertEquals(SaleStatus.OPENED, result.getStatus())
            );

            verify(repository).save(result);
        }

        @Test
        @DisplayName("Deve rejeitar a abertura quando o caixa não existir")
        void deveRejeitarAberturaQuandoCaixaNaoExistir() {
            // Arrange
            SaleOpenRequest request = createOpenRequest(null);
            when(cashDailyRepository.findById(CASH_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert — assertThrows executa e verifica a exceção
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.open(request)
            );

            verify(repository, never()).save(any(Sale.class));
            verifyNoInteractions(employeeRepository, customerRepository);
        }

        @Test
        @DisplayName("Deve rejeitar a abertura quando o funcionário não existir")
        void deveRejeitarAberturaQuandoFuncionarioNaoExistir() {
            // Arrange
            SaleOpenRequest request = createOpenRequest(null);

            when(cashDailyRepository.findById(CASH_ID))
                    .thenReturn(Optional.of(new CashDailyLog()));
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.open(request)
            );

            verify(repository, never()).save(any(Sale.class));
            verifyNoInteractions(customerRepository);
        }

        @Test
        @DisplayName("Deve rejeitar a abertura quando o cliente informado não existir")
        void deveRejeitarAberturaQuandoClienteNaoExistir() {
            // Arrange
            SaleOpenRequest request = createOpenRequest(CUSTOMER_ID);

            when(cashDailyRepository.findById(CASH_ID))
                    .thenReturn(Optional.of(new CashDailyLog()));
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(new Employee()));
            when(customerRepository.findById(CUSTOMER_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.open(request)
            );

            verify(repository, never()).save(any(Sale.class));
        }
    }

    @Nested
    @DisplayName("findAll — consulta das vendas")
    class FindAll {
        @Test
        @DisplayName("Deve retornar as vendas fornecidas pelo repositório")
        void deveRetornarVendasQuandoExistiremRegistros() {
            // Arrange
            Sale firstSale = createSale(SaleStatus.OPENED, ZERO);
            Sale secondSale = createSale(SaleStatus.COMPLETED, SALE_TOTAL);
            secondSale.setId(6L);

            when(repository.findAll())
                    .thenReturn(List.of(firstSale, secondSale));

            // Act
            List<Sale> result = service.findAll();

            // Assert — não estabelece uma ordenação de negócio para findAll
            assertEquals(List.of(firstSale, secondSale), result);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver vendas")
        void deveRetornarListaVaziaQuandoNaoHouverVendas() {
            // Arrange
            when(repository.findAll()).thenReturn(List.of());

            // Act
            List<Sale> result = service.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("addItems — inclusão de itens")
    class AddItems {
        @ParameterizedTest(name = "Desconto: {0}; total esperado: {1}")
        @CsvSource({"0.00, 30.00", "2.00, 28.00"})
        @DisplayName("Deve somar os novos itens, aplicar desconto e vincular associações")
        void deveAdicionarItensNaPrimeiraInclusao(
                BigDecimal discount,
                BigDecimal expectedTotal
        ) {
            // Arrange — venda sem itens e sem desconto anterior
            Sale sale = createSale(SaleStatus.OPENED, ZERO);
            SaleAddRequest request = createAddRequest(discount);
            Product product = createProduct();
            List<BatchConsumption> consumptions = createConsumptions();
            SaleItem firstItem = createItem("10.00");
            SaleItem secondItem = createItem("20.00");
            StockMovement movement = new StockMovement();

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));
            stubExistingProduct(product);
            when(batchService.fefo(PRODUCT_ID, 3))
                    .thenReturn(consumptions);
            when(itemService.save(consumptions, product, sale))
                    .thenReturn(List.of(firstItem, secondItem));
            when(movementService.saveMovementSale(consumptions, sale))
                    .thenReturn(List.of(movement));

            // Act
            String result = service.addItems(request);

            // Assert
            assertAll(
                    () -> assertEquals("Item adicionado com sucesso!", result),
                    () -> assertMoneyEquals(SALE_TOTAL, sale.getSubtotal()),
                    () -> assertMoneyEquals(discount, sale.getDiscount()),
                    () -> assertMoneyEquals(expectedTotal, sale.getTotal()),
                    () -> assertEquals(
                            List.of(firstItem, secondItem),
                            sale.getSaleItems()
                    ),
                    () -> assertEquals(
                            List.of(movement),
                            sale.getStockMovements()
                    ),
                    () -> assertSame(sale, firstItem.getSale()),
                    () -> assertSame(sale, secondItem.getSale()),
                    () -> assertSame(sale, movement.getSale())
            );

            verify(repository).save(sale);
        }

        @Test
        @DisplayName("Deve rejeitar a inclusão quando a venda não existir")
        void deveRejeitarInclusaoQuandoVendaNaoExistir() {
            // Arrange
            SaleAddRequest request = createAddRequest(ZERO);
            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.addItems(request)
            );

            verify(repository, never()).save(any(Sale.class));
            verifyNoInteractions(
                    productRepository, batchService, itemService, movementService
            );
        }

        @ParameterizedTest(name = "Status: {0}")
        @EnumSource(
                value = SaleStatus.class,
                names = {"COMPLETED", "CANCELLED"}
        )
        @DisplayName("Deve rejeitar a inclusão em venda concluída ou cancelada")
        void deveRejeitarInclusaoQuandoVendaNaoEstiverAberta(SaleStatus status) {
            // Arrange
            Sale sale = createSale(status, SALE_TOTAL);
            SaleAddRequest request = createAddRequest(ZERO);

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.addItems(request)
            );

            assertEquals(status, sale.getStatus());
            verify(repository, never()).save(any(Sale.class));
            verifyNoInteractions(
                    productRepository, batchService, itemService, movementService
            );
        }

        @Test
        @DisplayName("Deve rejeitar código de barras inexistente sem consumir estoque")
        void deveRejeitarInclusaoQuandoProdutoNaoExistir() {
            // Arrange
            Sale sale = createSale(SaleStatus.OPENED, ZERO);
            SaleAddRequest request = createAddRequest(ZERO);

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));
            when(productRepository.existsByBarcode(BARCODE))
                    .thenReturn(false);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.addItems(request)
            );

            verify(repository, never()).save(any(Sale.class));
            verifyNoInteractions(batchService, itemService, movementService);
        }

        @Test
        @DisplayName("Deve propagar falha no consumo de lotes sem salvar itens ou venda")
        void deveInterromperInclusaoQuandoConsumoDeLotesFalhar() {
            // Arrange
            Sale sale = createSale(SaleStatus.OPENED, ZERO);
            SaleAddRequest request = createAddRequest(ZERO);
            Product product = createProduct();
            ConflictException failure =
                    new ConflictException("Estoque insuficiente");

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));
            stubExistingProduct(product);
            when(batchService.fefo(PRODUCT_ID, 3))
                    .thenThrow(failure);

            // Act + Assert
            ConflictException result = assertThrows(
                    ConflictException.class,
                    () -> service.addItems(request)
            );

            assertSame(failure, result);
            verify(repository, never()).save(any(Sale.class));
            verifyNoInteractions(itemService, movementService);
        }

        @Test
        @DisplayName("Deve propagar falha ao salvar itens sem registrar movimentos ou salvar venda")
        void deveInterromperInclusaoQuandoGravacaoDosItensFalhar() {
            // Arrange
            Sale sale = createSale(SaleStatus.OPENED, ZERO);
            SaleAddRequest request = createAddRequest(ZERO);
            Product product = createProduct();
            List<BatchConsumption> consumptions = createConsumptions();
            IllegalStateException failure =
                    new IllegalStateException("Falha ao salvar itens");

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));
            stubExistingProduct(product);
            when(batchService.fefo(PRODUCT_ID, 3))
                    .thenReturn(consumptions);
            when(itemService.save(consumptions, product, sale))
                    .thenThrow(failure);

            // Act + Assert
            IllegalStateException result = assertThrows(
                    IllegalStateException.class,
                    () -> service.addItems(request)
            );

            assertSame(failure, result);
            verify(repository, never()).save(any(Sale.class));
            verifyNoInteractions(movementService);
        }

        @Test
        @DisplayName("Deve propagar falha nas movimentações sem salvar a venda")
        void deveInterromperInclusaoQuandoRegistroDeMovimentosFalhar() {
            // Arrange
            Sale sale = createSale(SaleStatus.OPENED, ZERO);
            SaleAddRequest request = createAddRequest(ZERO);
            Product product = createProduct();
            List<BatchConsumption> consumptions = createConsumptions();
            IllegalStateException failure =
                    new IllegalStateException("Falha na movimentação");

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));
            stubExistingProduct(product);
            when(batchService.fefo(PRODUCT_ID, 3))
                    .thenReturn(consumptions);
            when(itemService.save(consumptions, product, sale))
                    .thenReturn(List.of(createItem("30.00")));
            when(movementService.saveMovementSale(consumptions, sale))
                    .thenThrow(failure);

            // Act + Assert
            IllegalStateException result = assertThrows(
                    IllegalStateException.class,
                    () -> service.addItems(request)
            );

            assertSame(failure, result);
            verify(repository, never()).save(any(Sale.class));
        }
    }

    @Nested
    @DisplayName("close — fechamento da venda")
    class Close {
        @ParameterizedTest(name = "Pagamento: {0}")
        @EnumSource(PaymentMethod.class)
        @DisplayName("Deve concluir a venda e encaminhar valor e pagamento ao caixa")
        void deveFecharVendaComPagamentoValido(PaymentMethod paymentMethod) {
            // Arrange
            Sale sale = createSale(SaleStatus.OPENED, SALE_TOTAL);
            SaleCloseRequest request =
                    createCloseRequest(SALE_TOTAL, paymentMethod);

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));
            when(repository.save(sale)).thenReturn(sale);

            // Act
            Sale result = service.close(request, SALE_ID);

            // Assert
            assertAll(
                    () -> assertSame(sale, result),
                    () -> assertEquals(SaleStatus.COMPLETED, result.getStatus()),
                    () -> assertEquals(paymentMethod, result.getPaymentMethod()),
                    () -> assertMoneyEquals(SALE_TOTAL, result.getTotal())
            );

            verify(cashService)
                    .addAmountToCash(CASH_ID, SALE_TOTAL, paymentMethod);
            verify(repository).save(sale);
        }

        @Test
        @DisplayName("Deve rejeitar o fechamento de venda inexistente sem atualizar o caixa")
        void deveRejeitarFechamentoQuandoVendaNaoExistir() {
            // Arrange
            SaleCloseRequest request =
                    createCloseRequest(SALE_TOTAL, PaymentMethod.CASH);

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.close(request, SALE_ID)
            );

            verifyNoInteractions(cashService);
            verify(repository, never()).save(any(Sale.class));
        }

        @ParameterizedTest(name = "Valor recebido: {0}; total: 30.00")
        @ValueSource(strings = {"25.00", "35.00"})
        @DisplayName("Deve rejeitar valor diferente do total e preservar a venda")
        void deveRejeitarFechamentoQuandoValorDiferirDoTotal(String receivedValue) {
            // Arrange — caracteriza a regra atual de pagamento exato
            Sale sale = createSale(SaleStatus.OPENED, SALE_TOTAL);
            SaleCloseRequest request = createCloseRequest(
                    new BigDecimal(receivedValue),
                    PaymentMethod.CASH
            );

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.close(request, SALE_ID)
            );

            assertAll(
                    () -> assertEquals(SaleStatus.OPENED, sale.getStatus()),
                    () -> assertNull(sale.getPaymentMethod()),
                    () -> assertMoneyEquals(SALE_TOTAL, sale.getTotal())
            );

            verifyNoInteractions(cashService);
            verify(repository, never()).save(any(Sale.class));
        }

        @Test
        @DisplayName("Deve propagar recusa do caixa sem chamar save da venda")
        void deveInterromperFechamentoQuandoCaixaRecusarLancamento() {
            // Arrange
            Sale sale = createSale(SaleStatus.OPENED, SALE_TOTAL);
            SaleCloseRequest request =
                    createCloseRequest(SALE_TOTAL, PaymentMethod.CASH);
            ConflictException failure = new ConflictException("Caixa fechado");

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));
            doThrow(failure).when(cashService)
                    .addAmountToCash(CASH_ID, SALE_TOTAL, PaymentMethod.CASH);

            // Act + Assert — este teste não comprova rollback de banco
            ConflictException result = assertThrows(
                    ConflictException.class,
                    () -> service.close(request, SALE_ID)
            );

            assertSame(failure, result);
            verify(repository, never()).save(any(Sale.class));
        }

        @ParameterizedTest(name = "Deve rejeitar fechamento com status {0}")
        @EnumSource(
                value = SaleStatus.class,
                names = {"COMPLETED", "CANCELLED"}
        )
        void deveRejeitarFechamentoQuandoVendaNaoEstiverAberta(
                SaleStatus status
        ) {
            // Arrange
            Sale sale = createSale(status, SALE_TOTAL);
            SaleCloseRequest request =
                    createCloseRequest(SALE_TOTAL, PaymentMethod.CASH);

            when(repository.findById(SALE_ID))
                    .thenReturn(Optional.of(sale));

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.close(request, SALE_ID)
            );

            assertEquals(status, sale.getStatus());
            verifyNoInteractions(cashService);
            verify(repository, never()).save(any(Sale.class));
        }
    }

    // Helpers: dados padrão explícitos; cada teste configura o necessário.

    private SaleOpenRequest createOpenRequest(Long customerId) {
        SaleOpenRequest request = new SaleOpenRequest();
        request.setCustomerId(customerId);
        request.setEmployeeId(EMPLOYEE_ID);
        request.setCashId(CASH_ID);
        return request;
    }

    private SaleAddRequest createAddRequest(BigDecimal discount) {
        SaleAddRequest request = new SaleAddRequest();
        request.setId(SALE_ID);
        request.setCashId(CASH_ID);
        request.setBarcode(BARCODE);
        request.setDiscountUnit(discount);
        request.setQuantity(3);
        return request;
    }

    private SaleCloseRequest createCloseRequest(
            BigDecimal received,
            PaymentMethod method
    ) {
        SaleCloseRequest request = new SaleCloseRequest();
        request.setCashId(CASH_ID);
        request.setValueArrived(received);
        request.setPaymentMethod(method);
        return request;
    }

    private Sale createSale(SaleStatus status, BigDecimal total) {
        Sale sale = new Sale();
        sale.setId(SALE_ID);
        sale.setSubtotal(total);
        sale.setDiscount(ZERO);
        sale.setTotal(total);
        sale.setStatus(status);

        // Fixture de venda carregada; não comprova inicialização em open().
        sale.setSaleItems(new ArrayList<>());
        sale.setStockMovements(new ArrayList<>());

        return sale;
    }

    private Product createProduct() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setBarcode(BARCODE);
        product.setSalePrice(new BigDecimal("10.00"));
        return product;
    }

    private SaleItem createItem(String total) {
        SaleItem item = new SaleItem();
        item.setTotal(new BigDecimal(total));
        return item;
    }

    private List<BatchConsumption> createConsumptions() {
        return List.of(
                new BatchConsumption(10L, "LOTE-A", 1),
                new BatchConsumption(11L, "LOTE-B", 2)
        );
    }

    private void stubExistingProduct(Product product) {
        when(productRepository.existsByBarcode(BARCODE)).thenReturn(true);
        when(productRepository.findByBarcode(BARCODE)).thenReturn(product);
    }

    private static void assertMoneyEquals(
            BigDecimal expected,
            BigDecimal actual
    ) {
        assertNotNull(actual, "O valor monetário não deveria ser nulo");
        assertEquals(
                0,
                expected.compareTo(actual),
                () -> "Esperado: " + expected + ", recebido: " + actual
        );
    }
}