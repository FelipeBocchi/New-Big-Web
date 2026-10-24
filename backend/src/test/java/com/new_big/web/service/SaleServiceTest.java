
package com.new_big.web.service;

import com.new_big.web.controller.batch.dto.BatchConsumption;
import com.new_big.web.controller.saleController.dto.SaleAddRequest;
import com.new_big.web.controller.saleController.dto.SaleCloseRequest;
import com.new_big.web.controller.saleController.dto.SaleOpenRequest;
import com.new_big.web.entity.*;
import com.new_big.web.enums.PaymentMethod;
import com.new_big.web.enums.SaleStatus;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class SaleServiceTest {

    private static final Long SALE_ID = 1L;
    private static final Long CASH_ID = 2L;
    private static final Long EMPLOYEE_ID = 3L;
    private static final Long CUSTOMER_ID = 4L;
    private static final Long PRODUCT_ID = 5L;

    private static final String BARCODE = "7891234567890";

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private static final BigDecimal SALE_TOTAL =
            new BigDecimal("30.00");

    @Mock
    private SaleRepository repository;

    @Mock
    private CashDailyRepository cashDailyRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BatchService batchService;

    @Mock
    private StockMovementService movementService;

    @Mock
    private SaleItemService itemService;

    @Mock
    private CashDailyService cashService;

    @InjectMocks
    private SaleService service;

    private Employee employee;
    private CashDailyLog cash;
    private Customer customer;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        cash = new CashDailyLog();
        customer = new Customer();
    }

    // ==========================================
    // OPEN
    // ==========================================

    @Test
    void caso_Abrir_Venda_Quando_Caixa_E_Funcionario_Existirem() {

        // Given — existe um caixa e um funcionário.
        SaleOpenRequest request = createOpenRequest(
                null,
                EMPLOYEE_ID,
                CASH_ID
        );

        when(cashDailyRepository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(employee));

        when(repository.save(any(Sale.class)))
                .thenAnswer(invocation ->
                        invocation.<Sale>getArgument(0)
                );

        // When — o serviço abre uma nova venda.
        Sale result = service.open(request);

        // Then — a venda começa aberta, sem cliente e sem valores.
        assertAll(
                () -> assertMoneyEquals(
                        ZERO,
                        result.getSubtotal()
                ),
                () -> assertMoneyEquals(
                        ZERO,
                        result.getDiscount()
                ),
                () -> assertMoneyEquals(
                        ZERO,
                        result.getTotal()
                ),
                () -> assertEquals(
                        SaleStatus.OPENED,
                        result.getStatus()
                ),
                () -> assertNull(result.getPaymentMethod()),
                () -> assertNull(result.getCustomer()),
                () -> assertSame(employee, result.getEmployee()),
                () -> assertSame(cash, result.getCashier())
        );

        verify(cashDailyRepository).findById(CASH_ID);
        verify(employeeRepository).findById(EMPLOYEE_ID);
        verify(repository).save(result);
        verifyNoInteractions(customerRepository);
    }

    @Test
    void caso_Abrir_Venda_Quando_Cliente_Existir() {

        // Given — o caixa, o funcionário e o cliente existem.
        SaleOpenRequest request = createOpenRequest(
                CUSTOMER_ID,
                EMPLOYEE_ID,
                CASH_ID
        );

        when(cashDailyRepository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(employee));

        when(customerRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(customer));

        when(repository.save(any(Sale.class)))
                .thenAnswer(invocation ->
                        invocation.<Sale>getArgument(0)
                );

        // When — o serviço abre a venda vinculada ao cliente.
        Sale result = service.open(request);

        // Then — o cliente deve estar associado à venda.
        assertAll(
                () -> assertSame(customer, result.getCustomer()),
                () -> assertSame(employee, result.getEmployee()),
                () -> assertSame(cash, result.getCashier()),
                () -> assertEquals(
                        SaleStatus.OPENED,
                        result.getStatus()
                )
        );

        verify(customerRepository).findById(CUSTOMER_ID);
        verify(repository).save(result);
    }

    @Test
    void caso_Abrir_Venda_Quando_Caixa_Nao_Existir() {

        // Given — o caixa informado não existe.
        SaleOpenRequest request = createOpenRequest(
                null,
                EMPLOYEE_ID,
                CASH_ID
        );

        when(cashDailyRepository.findById(CASH_ID))
                .thenReturn(Optional.empty());

        // When — o serviço tenta abrir a venda.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.open(request)
        );

        // Then — a abertura deve ser interrompida.
        assertEquals(
                "Caixa não emcontrado com o id",
                exception.getMessage()
        );

        verify(cashDailyRepository).findById(CASH_ID);
        verifyNoInteractions(employeeRepository, customerRepository);
        verify(repository, never()).save(any(Sale.class));
    }

    @Test
    void caso_Abrir_Venda_Quando_Funcionario_Nao_Existir() {

        // Given — o caixa existe, mas o funcionário não.
        SaleOpenRequest request = createOpenRequest(
                null,
                EMPLOYEE_ID,
                CASH_ID
        );

        when(cashDailyRepository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        // When — o serviço tenta abrir a venda.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.open(request)
        );

        // Then — nenhuma venda deve ser salva.
        assertEquals(
                "Funcionário não emcontrado com o id",
                exception.getMessage()
        );

        verify(cashDailyRepository).findById(CASH_ID);
        verify(employeeRepository).findById(EMPLOYEE_ID);
        verifyNoInteractions(customerRepository);
        verify(repository, never()).save(any(Sale.class));
    }

    @Test
    void caso_Abrir_Venda_Quando_Cliente_Nao_Existir() {

        // Given — caixa e funcionário existem, mas o cliente não.
        SaleOpenRequest request = createOpenRequest(
                CUSTOMER_ID,
                EMPLOYEE_ID,
                CASH_ID
        );

        when(cashDailyRepository.findById(CASH_ID))
                .thenReturn(Optional.of(cash));

        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(employee));

        when(customerRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.empty());

        // When — o serviço tenta abrir a venda.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.open(request)
        );

        // Then — a venda não deve ser salva.
        assertEquals(
                "Cliente não emcontrado com o id",
                exception.getMessage()
        );

        verify(customerRepository).findById(CUSTOMER_ID);
        verify(repository, never()).save(any(Sale.class));
    }

    // ==========================================
    // FIND ALL
    // ==========================================

    @Test
    void caso_Buscar_Todas_As_Vendas_Quando_Existirem_Registros() {

        // Given — existem duas vendas cadastradas.
        Sale firstSale = createSale(
                SaleStatus.OPENED,
                ZERO,
                ZERO,
                ZERO
        );

        Sale secondSale = createSale(
                SaleStatus.COMPLETED,
                new BigDecimal("20.00"),
                ZERO,
                new BigDecimal("20.00")
        );

        when(repository.findAll())
                .thenReturn(List.of(firstSale, secondSale));

        // When — o serviço consulta todas as vendas.
        List<Sale> result = service.findAll();

        // Then — as vendas devem ser retornadas na ordem recebida.
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertSame(firstSale, result.get(0)),
                () -> assertSame(secondSale, result.get(1))
        );

        verify(repository).findAll();
    }

    // ==========================================
    // ADD ITEMS
    // ==========================================

    @Test
    void caso_Adicionar_Itens_Quando_Venda_E_Produto_Existirem() {

        // Given — existe uma venda aberta e o produto existe.
        Sale sale = createSale(
                SaleStatus.OPENED,
                ZERO,
                new BigDecimal("3.00"),
                ZERO
        );

        SaleAddRequest request = createAddRequest(
                SALE_ID,
                CASH_ID,
                BARCODE,
                new BigDecimal("2.00"),
                2
        );

        Product product = mock(Product.class);
        when(product.getId()).thenReturn(PRODUCT_ID);

        List<BatchConsumption> consumptions = List.of();

        SaleItem firstItem = mock(SaleItem.class);
        SaleItem secondItem = mock(SaleItem.class);

        when(firstItem.getTotal())
                .thenReturn(new BigDecimal("10.00"));

        when(secondItem.getTotal())
                .thenReturn(new BigDecimal("20.00"));

        List<SaleItem> items = List.of(firstItem, secondItem);

        StockMovement movement = mock(StockMovement.class);
        List<StockMovement> movements = List.of(movement);

        when(repository.findById(SALE_ID))
                .thenReturn(Optional.of(sale));

        when(productRepository.existsByBarcode(BARCODE))
                .thenReturn(true);

        when(productRepository.findByBarcode(BARCODE))
                .thenReturn(product);

        when(batchService.fefo(PRODUCT_ID, 2))
                .thenReturn(consumptions);

        when(itemService.save(consumptions, product, sale))
                .thenReturn(items);

        when(movementService.saveMovementSale(consumptions, sale))
                .thenReturn(movements);

        when(repository.save(sale))
                .thenReturn(sale);

        // When — o serviço adiciona os itens à venda.
        String result = service.addItems(request);

        // Then — os valores e as associações devem ser atualizados.
        assertAll(
                () -> assertEquals(
                        "Item adicionado com sucesso!",
                        result
                ),
                () -> assertMoneyEquals(
                        new BigDecimal("30.00"),
                        sale.getSubtotal()
                ),
                () -> assertMoneyEquals(
                        new BigDecimal("5.00"),
                        sale.getDiscount()
                ),
                () -> assertMoneyEquals(
                        new BigDecimal("28.00"),
                        sale.getTotal()
                ),
                () -> assertEquals(2, sale.getSaleItems().size()),
                () -> assertEquals(
                        1,
                        sale.getStockMovements().size()
                ),
                () -> assertSame(
                        firstItem,
                        sale.getSaleItems().get(0)
                ),
                () -> assertSame(
                        movement,
                        sale.getStockMovements().get(0)
                )
        );

        verify(repository).findById(SALE_ID);
        verify(productRepository).existsByBarcode(BARCODE);
        verify(productRepository).findByBarcode(BARCODE);
        verify(batchService).fefo(PRODUCT_ID, 2);
        verify(itemService).save(consumptions, product, sale);
        verify(movementService).saveMovementSale(consumptions, sale);
        verify(firstItem).setSale(sale);
        verify(secondItem).setSale(sale);
        verify(movement).setSale(sale);
        verify(repository).save(sale);
    }

    @Test
    void caso_Adicionar_Itens_Quando_Venda_Nao_Existir() {

        // Given — o identificador da venda não existe.
        SaleAddRequest request = createAddRequest(
                SALE_ID,
                CASH_ID,
                BARCODE,
                ZERO,
                1
        );

        when(repository.findById(SALE_ID))
                .thenReturn(Optional.empty());

        // When — o serviço tenta adicionar itens.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.addItems(request)
        );

        // Then — a operação deve ser interrompida.
        assertEquals(
                "Venda não encontrada com o id",
                exception.getMessage()
        );

        verify(repository).findById(SALE_ID);
        verifyNoInteractions(productRepository, batchService,
                itemService, movementService);
        verify(repository, never()).save(any(Sale.class));
    }

    @Test
    void caso_Adicionar_Itens_Quando_Venda_Nao_Estiver_Aberta() {

        // Given — a venda já foi concluída.
        Sale sale = createSale(
                SaleStatus.COMPLETED,
                SALE_TOTAL,
                ZERO,
                SALE_TOTAL
        );

        SaleAddRequest request = createAddRequest(
                SALE_ID,
                CASH_ID,
                BARCODE,
                ZERO,
                1
        );

        when(repository.findById(SALE_ID))
                .thenReturn(Optional.of(sale));

        // When — o serviço tenta adicionar novos itens.
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.addItems(request)
        );

        // Then — a alteração deve ser rejeitada.
        assertEquals("Venda não está aberta!", exception.getMessage());

        verify(repository).findById(SALE_ID);
        verifyNoInteractions(productRepository, batchService,
                itemService, movementService);
        verify(repository, never()).save(any(Sale.class));
    }

    @Test
    void caso_Adicionar_Itens_Quando_CodigoDeBarras_Nao_Existir() {

        // Given — a venda está aberta, mas o produto não existe.
        Sale sale = createSale(
                SaleStatus.OPENED,
                ZERO,
                ZERO,
                ZERO
        );

        SaleAddRequest request = createAddRequest(
                SALE_ID,
                CASH_ID,
                BARCODE,
                ZERO,
                1
        );

        when(repository.findById(SALE_ID))
                .thenReturn(Optional.of(sale));

        when(productRepository.existsByBarcode(BARCODE))
                .thenReturn(false);

        // When — o serviço tenta localizar o produto.
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> service.addItems(request)
        );

        // Then — a operação deve ser rejeitada sem salvar a venda.
        assertEquals(
                "Código de barro informado não existe no banco!",
                exception.getMessage()
        );

        verify(repository).findById(SALE_ID);
        verify(productRepository).existsByBarcode(BARCODE);
        verify(productRepository, never()).findByBarcode(anyString());
        verifyNoInteractions(batchService, itemService, movementService);
        verify(repository, never()).save(any(Sale.class));
    }

    // ==========================================
    // CLOSE
    // ==========================================

    @Test
    void caso_Fechar_Venda_Quando_Valor_E_Pagamento_Forem_Validos() {

        // Given — existe uma venda cujo total é R$ 30,00.
        Sale sale = createSale(
                SaleStatus.OPENED,
                SALE_TOTAL,
                ZERO,
                SALE_TOTAL
        );

        SaleCloseRequest request = createCloseRequest(
                CASH_ID,
                SALE_TOTAL,
                PaymentMethod.CASH
        );

        when(repository.findById(SALE_ID))
                .thenReturn(Optional.of(sale));

        when(repository.save(sale))
                .thenReturn(sale);

        // When — o serviço fecha a venda.
        Sale result = service.close(request, SALE_ID);

        // Then — a venda é concluída e o caixa é atualizado.
        assertAll(
                () -> assertSame(sale, result),
                () -> assertEquals(
                        SaleStatus.COMPLETED,
                        result.getStatus()
                ),
                () -> assertEquals(
                        PaymentMethod.CASH,
                        result.getPaymentMethod()
                ),
                () -> assertMoneyEquals(
                        SALE_TOTAL,
                        result.getTotal()
                )
        );

        verify(repository).findById(SALE_ID);
        verify(cashService).addAmountToCash(
                CASH_ID,
                SALE_TOTAL,
                PaymentMethod.CASH
        );
        verify(repository).save(sale);
    }

    @Test
    void caso_Fechar_Venda_Quando_Venda_Nao_Existir() {

        // Given — não existe uma venda com o identificador informado.
        SaleCloseRequest request = createCloseRequest(
                CASH_ID,
                SALE_TOTAL,
                PaymentMethod.CASH
        );

        when(repository.findById(SALE_ID))
                .thenReturn(Optional.empty());

        // When — o serviço tenta fechar a venda.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.close(request, SALE_ID)
        );

        // Then — nenhuma atualização financeira deve ocorrer.
        assertEquals(
                "Venda não emcontrado com o id",
                exception.getMessage()
        );

        verify(repository).findById(SALE_ID);
        verifyNoInteractions(cashService);
        verify(repository, never()).save(any(Sale.class));
    }

    @Test
    void caso_Fechar_Venda_Quando_ValorRecebido_For_Diferente_Do_Total() {

        // Given — a venda totaliza R$ 30,00, mas o pagamento informado é R$ 25,00.
        Sale sale = createSale(
                SaleStatus.OPENED,
                SALE_TOTAL,
                ZERO,
                SALE_TOTAL
        );

        SaleCloseRequest request = createCloseRequest(
                CASH_ID,
                new BigDecimal("25.00"),
                PaymentMethod.CASH
        );

        when(repository.findById(SALE_ID))
                .thenReturn(Optional.of(sale));

        // When — o serviço tenta fechar a venda.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.close(request, SALE_ID)
        );

        // Then — o fechamento é rejeitado e o caixa não é atualizado.
        assertEquals(
                "Valor recebido não bate com o valor total da venda",
                exception.getMessage()
        );

        assertEquals(SaleStatus.OPENED, sale.getStatus());

        verify(repository).findById(SALE_ID);
        verifyNoInteractions(cashService);
        verify(repository, never()).save(any(Sale.class));
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private SaleOpenRequest createOpenRequest(
            Long customerId,
            Long employeeId,
            Long cashId
    ) {
        SaleOpenRequest request = new SaleOpenRequest();

        request.setCustomerId(customerId);
        request.setEmployeeId(employeeId);
        request.setCashId(cashId);

        return request;
    }

    private SaleAddRequest createAddRequest(
            Long saleId,
            Long cashId,
            String barcode,
            BigDecimal discountUnit,
            int quantity
    ) {
        SaleAddRequest request = new SaleAddRequest();

        request.setId(saleId);
        request.setCashId(cashId);
        request.setBarcode(barcode);
        request.setDiscountUnit(discountUnit);
        request.setQuantity(quantity);

        return request;
    }

    private SaleCloseRequest createCloseRequest(
            Long cashId,
            BigDecimal valueArrived,
            PaymentMethod paymentMethod
    ) {
        SaleCloseRequest request = new SaleCloseRequest();

        request.setCashId(cashId);
        request.setValueArrived(valueArrived);
        request.setPaymentMethod(paymentMethod);

        return request;
    }

    private Sale createSale(
            SaleStatus status,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal total
    ) {
        Sale sale = new Sale();

        sale.setId(SALE_ID);
        sale.setSubtotal(subtotal);
        sale.setDiscount(discount);
        sale.setTotal(total);
        sale.setStatus(status);

        // As coleções precisam existir para os métodos addSaleItem
        // e addStockMovement adicionarem elementos.
        sale.setSaleItems(new ArrayList<>());
        sale.setStockMovements(new ArrayList<>());

        return sale;
    }

    private static void assertMoneyEquals(
            BigDecimal expected,
            BigDecimal actual
    ) {
        assertNotNull(
                actual,
                "O valor monetário não deveria ser nulo."
        );

        assertEquals(
                0,
                expected.compareTo(actual),
                () -> "Esperado: " + expected + ", recebido: " + actual
        );
    }
}
