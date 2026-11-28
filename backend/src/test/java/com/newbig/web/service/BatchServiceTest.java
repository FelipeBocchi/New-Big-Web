package com.newbig.web.service;

import com.newbig.web.controller.batch.dto.BatchConsumption;
import com.newbig.web.controller.batch.dto.BatchRequest;
import com.newbig.web.entity.Batch;
import com.newbig.web.entity.Product;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.BatchRepository;
import com.newbig.web.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchService — testes unitários")
class BatchServiceTest {
    private static final Long ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Mock
    private BatchRepository repository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementService movementService;

    @InjectMocks
    private BatchService service;

    @Spy
    private Clock clock = Clock.fixed(
            TODAY.atStartOfDay(ZONE).toInstant(),
            ZONE
    );

    private MockedStatic<LocalDate> dates;

    @BeforeEach
    void fixarData() {
        dates = mockStatic(LocalDate.class, CALLS_REAL_METHODS);
        dates.when(LocalDate::now).thenReturn(TODAY);
        dates.when(() -> LocalDate.now(ZONE)).thenReturn(TODAY);
    }

    @AfterEach
    void liberarData() {
        dates.close();
    }

    @Nested
    @DisplayName("save — cadastro")
    class Save {
        @Test
        @DisplayName("Deve salvar dados e inicializar indicadores do lote")
        void deveSalvar() {
            // Arrange
            BatchRequest request = request();
            Product product = product();

            when(repository.existsByBatchCode("L-A"))
                    .thenReturn(false);
            when(productRepository.findById(PRODUCT_ID))
                    .thenReturn(Optional.of(product));
            when(repository.save(any(Batch.class)))
                    .thenAnswer(i -> i.<Batch>getArgument(0));

            // Act
            Batch result = service.save(request);

            // Assert
            assertFields(request, result, product);

            assertAll(
                    () -> assertEquals(TODAY, result.getCreatedAt()),
                    () -> assertEquals(
                            Boolean.FALSE,
                            result.getExpiration()
                    ),
                    () -> assertEquals(
                            Boolean.FALSE,
                            result.getQuantityIsZero()
                    )
            );

            verify(repository).save(result);
            verify(movementService).saveMovementEntry(result, 3L);
        }

        @Test
        @DisplayName("Deve rejeitar código de lote duplicado")
        void deveRejeitarDuplicidade() {
            // Arrange
            BatchRequest request = request();

            when(repository.existsByBatchCode("L-A"))
                    .thenReturn(true);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.save(request)
            );

            verify(repository, never()).save(any(Batch.class));
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Deve rejeitar cadastro com produto inexistente")
        void deveRejeitarProdutoAusente() {
            // Arrange
            BatchRequest request = request();

            when(repository.existsByBatchCode("L-A"))
                    .thenReturn(false);
            when(productRepository.findById(PRODUCT_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.save(request)
            );

            verify(repository, never()).save(any(Batch.class));
        }

        // new test
        @ParameterizedTest(name = "Quantidade inválida no cadastro: {0}")
        @NullSource
        @ValueSource(ints = {0, -1})
        @DisplayName("Deve rejeitar quantidade ausente, zero ou negativa no cadastro")
        void deveRejeitarQuantidadeInvalidaNoCadastro(Integer quantity) {
            // Arrange
            BatchRequest request = request();
            request.setQuantity(quantity);

            when(repository.existsByBatchCode(request.getBatchCode()))
                    .thenReturn(false);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.save(request)
            );

            verify(repository, never()).save(any(Batch.class));
            verifyNoInteractions(productRepository);
        }

        @ParameterizedTest(name = "Dias em relação a hoje: {0}")
        @NullSource
        @ValueSource(ints = {-1, 0})
        @DisplayName("Deve rejeitar validade ausente, passada ou igual a hoje no cadastro")
        void deveRejeitarValidadeInvalidaNoCadastro(Integer days) {
            // Arrange
            BatchRequest request = request();
            request.setExpirationAt(
                    days == null ? null : TODAY.plusDays(days)
            );

            when(repository.existsByBatchCode(request.getBatchCode()))
                    .thenReturn(false);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.save(request)
            );

            verify(repository, never()).save(any(Batch.class));
            verifyNoInteractions(productRepository);
        }

        @ParameterizedTest(name = "Quantidade inválida no FEFO: {0}")
        @ValueSource(ints = {0, -1})
        @DisplayName("Deve rejeitar quantidade zero ou negativa antes de consultar estoque")
        void deveRejeitarQuantidadeInvalidaNoFefo(int quantity) {
            // Arrange — entrada inválida não precisa de lotes configurados.

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.fefo(PRODUCT_ID, quantity)
            );

            verifyNoInteractions(repository, productRepository);
        }

        @Test
        @DisplayName("Deve rejeitar código de outro lote no PATCH sem salvar")
        void deveRejeitarCodigoDuplicadoNoPatch() {
            // Arrange
            Batch existing = batch(ID, "L-A", 3);

            BatchRequest patch = new BatchRequest();
            patch.setBatchCode("L-B");

            when(repository.findById(ID))
                    .thenReturn(Optional.of(existing));

            when(repository.existsByBatchCode("L-B"))
                    .thenReturn(true);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.updatePartial(patch, ID)
            );

            assertEquals("L-A", existing.getBatchCode());
            verify(repository, never()).save(any(Batch.class));
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Deve aceitar o próprio código no PATCH sem verificar duplicidade")
        void deveAceitarCodigoAtualNoPatch() {
            // Arrange
            Batch existing = batch(ID, "L-A", 3);

            BatchRequest patch = new BatchRequest();
            patch.setBatchCode("L-A");

            when(repository.findById(ID))
                    .thenReturn(Optional.of(existing));

            when(repository.save(existing))
                    .thenReturn(existing);

            // Act
            Batch result = service.updatePartial(patch, ID);

            // Assert
            assertSame(existing, result);
            assertEquals("L-A", existing.getBatchCode());

            verify(repository, never()).existsByBatchCode(anyString());
            verify(repository).save(existing);
        }

        @ParameterizedTest(name = "Dias: {0}; indicador anterior: {1}")
        @CsvSource(
                value = {
                        "-1,false",
                        "0,false",
                        "-1,NULL"
                },
                nullValues = "NULL"
        )
        @DisplayName("Deve marcar e salvar lote que atingiu a validade")
        void deveMarcarLoteVencido(int days, Boolean previousExpiration) {
            // Arrange
            Batch existing = batch(ID, "L-A", 3);
            existing.setExpirationAt(TODAY.plusDays(days));
            existing.setExpiration(previousExpiration);

            when(repository.findAll())
                    .thenReturn(List.of(existing));

            // Act
            service.toggleExpiration();

            // Assert
            assertEquals(Boolean.TRUE, existing.getExpiration());
            verify(repository).save(existing);
        }

        @Test
        @DisplayName("Deve ignorar lote sem data de validade sem lançar erro")
        void deveIgnorarLoteSemValidade() {
            // Arrange
            Batch existing = batch(ID, "L-A", 3);
            existing.setExpirationAt(null);

            when(repository.findAll())
                    .thenReturn(List.of(existing));

            // Act + Assert
            assertDoesNotThrow(() -> service.toggleExpiration());

            assertEquals(Boolean.FALSE, existing.getExpiration());
            verify(repository, never()).save(any(Batch.class));
        }

        @Test
        @DisplayName("Não deve salvar novamente lote já marcado como vencido")
        void naoDeveSalvarNovamenteLoteVencido() {
            // Arrange
            Batch existing = batch(ID, "L-A", 3);
            existing.setExpirationAt(TODAY.minusDays(1));
            existing.setExpiration(true);

            when(repository.findAll())
                    .thenReturn(List.of(existing));

            // Act
            service.toggleExpiration();

            // Assert
            assertEquals(Boolean.TRUE, existing.getExpiration());
            verify(repository, never()).save(any(Batch.class));
        }

        @Test
        @DisplayName("Deve marcar quantidade zero quando o FEFO esgotar o lote")
        void deveMarcarLoteEsgotadoNoFefo() {
            // Arrange
            Batch existing = batch(ID, "L-A", 3);

            when(repository.findAvailableBatchesFefo(PRODUCT_ID, TODAY))
                    .thenReturn(List.of(existing));

            // Act
            List<BatchConsumption> result = service.fefo(PRODUCT_ID, 3);

            // Assert
            assertEquals(
                    List.of(new BatchConsumption(ID, "L-A", 3)),
                    result
            );

            assertEquals(0, existing.getQuantity());
            assertEquals(Boolean.TRUE, existing.getQuantityIsZero());
        }
    }

    @Nested
    @DisplayName("Consultas")
    class Queries {
        @Test
        @DisplayName("Deve retornar lote pelo ID")
        void deveBuscarPorId() {
            // Arrange
            Batch batch = batch(1L, "L-A", 3);

            when(repository.findById(ID))
                    .thenReturn(Optional.of(batch));

            // Act
            Batch result = service.findById(ID);

            // Assert
            assertSame(batch, result);
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
            List<Batch> expected =
                    empty ? List.of() : List.of(batch(1L, "L-A", 3));

            when(repository.findAll()).thenReturn(expected);

            // Act
            List<Batch> result = service.findAll();

            // Assert
            assertEquals(expected, result);
        }

        @ParameterizedTest(name = "Expirado: {0}; lista vazia: {1}")
        @CsvSource({
                "true,false",
                "false,false",
                "true,true",
                "false,true"
        })
        @DisplayName("Deve filtrar validade e rejeitar lista vazia — contrato atual")
        void deveFiltrarValidade(boolean expired, boolean empty) {
            // Arrange
            Batch batch = batch(1L, "L-A", 3);
            batch.setExpiration(expired);

            when(repository.findByExpiration(expired))
                    .thenReturn(empty ? List.of() : List.of(batch));

            // Act + Assert
            if (empty) {
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.findByExpiration(expired)
                );
            } else {
                assertEquals(
                        List.of(batch),
                        service.findByExpiration(expired)
                );
            }
        }
    }

    @Nested
    @DisplayName("update e updatePartial — alterações")
    class Updates {
        @ParameterizedTest(name = "Alterar código: {0}")
        @ValueSource(booleans = {false, true})
        @DisplayName("Deve atualizar lote com código atual ou novo e livre")
        void deveAtualizar(boolean changeCode) {
            // Arrange
            Batch batch = batch(1L, "L-A", 3);
            BatchRequest request = request();
            request.setQuantity(8);
            request.setExpirationAt(TODAY.plusDays(60));

            if (changeCode) request.setBatchCode("L-B");

            Product product = product();

            when(repository.findById(ID))
                    .thenReturn(Optional.of(batch));

            if (changeCode) {
                when(repository.existsByBatchCode("L-B"))
                        .thenReturn(false);
            }

            when(productRepository.findById(PRODUCT_ID))
                    .thenReturn(Optional.of(product));
            when(repository.save(batch)).thenReturn(batch);

            // Act
            Batch result = service.update(request, ID);

            // Assert
            assertSame(batch, result);
            assertFields(request, batch, product);
            verify(repository).save(batch);

            if (!changeCode) {
                verify(repository, never())
                        .existsByBatchCode(anyString());
            }
        }

        @Test
        @DisplayName("Deve rejeitar atualização com código de outro lote")
        void deveRejeitarDuplicidade() {
            // Arrange
            Batch batch = batch(1L, "L-A", 3);
            BatchRequest request = request();
            request.setBatchCode("L-B");

            when(repository.findById(ID))
                    .thenReturn(Optional.of(batch));
            when(repository.existsByBatchCode("L-B"))
                    .thenReturn(true);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.update(request, ID)
            );

            verify(repository, never()).save(any(Batch.class));
            verifyNoInteractions(productRepository);
        }

        @ParameterizedTest(name = "PATCH: {0}")
        @ValueSource(booleans = {false, true})
        @DisplayName("Deve rejeitar alteração com produto inexistente")
        void deveRejeitarProdutoAusente(boolean partial) {
            // Arrange
            BatchRequest request = request();

            when(repository.findById(ID))
                    .thenReturn(Optional.of(batch(1L, "L-A", 3)));
            when(productRepository.findById(PRODUCT_ID))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                if (partial) service.updatePartial(request, ID);
                else service.update(request, ID);
            });

            verify(repository, never()).save(any(Batch.class));
        }

        @Test
        @DisplayName("Deve aplicar código, validade e produto presentes no PATCH")
        void deveAplicarPatch() {
            // Arrange
            Batch batch = batch(1L, "L-A", 3);
            BatchRequest request = request();
            request.setBatchCode("L-B");
            request.setExpirationAt(TODAY.plusDays(60));
            Product product = product();

            when(repository.findById(ID))
                    .thenReturn(Optional.of(batch));
            when(productRepository.findById(PRODUCT_ID))
                    .thenReturn(Optional.of(product));
            when(repository.save(batch)).thenReturn(batch);

            // Act
            Batch result = service.updatePartial(request, ID);

            // Assert
            assertSame(batch, result);
            assertEquals("L-B", batch.getBatchCode());
            assertEquals(
                    request.getExpirationAt(),
                    batch.getExpirationAt()
            );
            assertSame(product, batch.getProduct());
            verify(repository).save(batch);
        }

        @Test
        @DisplayName("Deve preservar campos omitidos no PATCH")
        void devePreservarCampos() {
            // Arrange
            Batch batch = batch(1L, "L-A", 3);
            Product originalProduct = batch.getProduct();
            BatchRequest patch = new BatchRequest();

            when(repository.findById(ID))
                    .thenReturn(Optional.of(batch));
            when(repository.save(batch)).thenReturn(batch);

            // Act
            service.updatePartial(patch, ID);

            // Assert
            assertAll(
                    () -> assertEquals("L-A", batch.getBatchCode()),
                    () -> assertEquals(3, batch.getQuantity()),
                    () -> assertEquals(
                            TODAY.plusDays(30),
                            batch.getExpirationAt()
                    ),
                    () -> assertSame(
                            originalProduct,
                            batch.getProduct()
                    )
            );

            verifyNoInteractions(productRepository);
            verify(repository).save(batch);
        }

        @ParameterizedTest(name = "PATCH: {0}")
        @ValueSource(booleans = {false, true})
        @DisplayName("Deve rejeitar alteração de lote inexistente")
        void deveRejeitarAusente(boolean partial) {
            // Arrange
            BatchRequest request = request();
            when(repository.findById(ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                if (partial) service.updatePartial(request, ID);
                else service.update(request, ID);
            });

            verify(repository, never()).save(any(Batch.class));
            verifyNoInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("fefo — consumo dos lotes fornecidos pelo repositório")
    class Fefo {
        static Stream<Arguments> consumptions() {
            return Stream.of(
                    Arguments.of(
                            2,
                            new int[]{1, 5, 9},
                            List.of(
                                    new BatchConsumption(1L, "L-A", 2)
                            )
                    ),
                    Arguments.of(
                            3,
                            new int[]{0, 5, 9},
                            List.of(
                                    new BatchConsumption(1L, "L-A", 3)
                            )
                    ),
                    Arguments.of(
                            7,
                            new int[]{0, 1, 9},
                            List.of(
                                    new BatchConsumption(1L, "L-A", 3),
                                    new BatchConsumption(2L, "L-B", 4)
                            )
                    ),
                    Arguments.of(
                            17,
                            new int[]{0, 0, 0},
                            List.of(
                                    new BatchConsumption(1L, "L-A", 3),
                                    new BatchConsumption(2L, "L-B", 5),
                                    new BatchConsumption(3L, "L-C", 9)
                            )
                    )
            );
        }

        @ParameterizedTest(name = "Quantidade solicitada: {0}")
        @MethodSource("consumptions")
        @DisplayName("Deve consumir na ordem recebida e preservar o saldo dos lotes")
        void deveConsumir(
                int quantity,
                int[] balances,
                List<BatchConsumption> expected
        ) {
            // Arrange
            List<Batch> batches = List.of(
                    batch(1L, "L-A", 3),
                    batch(2L, "L-B", 5),
                    batch(3L, "L-C", 9)
            );

            when(repository.findAvailableBatchesFefo(PRODUCT_ID, TODAY))
                    .thenReturn(batches);

            // Act
            List<BatchConsumption> result =
                    service.fefo(PRODUCT_ID, quantity);

            // Assert
            assertEquals(expected, result);

            assertArrayEquals(
                    balances,
                    batches.stream()
                            .mapToInt(Batch::getQuantity)
                            .toArray()
            );
        }

        @ParameterizedTest(name = "Sem lotes: {0}")
        @ValueSource(booleans = {false, true})
        @DisplayName("Deve rejeitar estoque insuficiente sem alterar quantidades")
        void deveRejeitarEstoqueInsuficiente(boolean empty) {
            // Arrange
            Batch batch = batch(1L, "L-A", 3);

            when(repository.findAvailableBatchesFefo(PRODUCT_ID, TODAY))
                    .thenReturn(empty ? List.of() : List.of(batch));

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.fefo(PRODUCT_ID, 4)
            );

            assertEquals(3, batch.getQuantity());
        }
    }

    @Nested
    @DisplayName("toggleExpiration — verificação de validade")
    class Expiration {
        @ParameterizedTest(name = "Indicador atual: {0}")
        @ValueSource(booleans = {false, true})
        @DisplayName("Não deve marcar lote com validade futura")
        void deveIgnorarValidadeFutura(boolean expired) {
            // Arrange
            Batch batch = batch(1L, "L-A", 3);
            batch.setExpiration(expired);

            when(repository.findAll()).thenReturn(List.of(batch));

            // Act
            service.toggleExpiration();

            // Assert
            assertEquals(expired, batch.getExpiration());
            verify(repository, never()).save(any(Batch.class));
        }

        @Test
        @DisplayName("Deve aceitar ausência de lotes na rotina de validade")
        void deveAceitarListaVazia() {
            // Arrange
            when(repository.findAll()).thenReturn(List.of());

            // Act + Assert
            assertDoesNotThrow(() -> service.toggleExpiration());
            verify(repository, never()).save(any(Batch.class));
        }
    }

    private Product product() {
        Product p = new Product();
        p.setId(PRODUCT_ID);
        return p;
    }


    private BatchRequest request() {
        BatchRequest r = new BatchRequest();
        r.setBatchCode("L-A");
        r.setProductId(PRODUCT_ID);
        r.setQuantity(3);
        r.setExpirationAt(TODAY.plusDays(30));
        r.setEmployeeId(3L);
        return r;
    }

    private Batch batch(Long id, String code, int quantity) {
        Batch b = new Batch();
        b.setId(id);
        b.setBatchCode(code);
        b.setQuantity(quantity);
        b.setExpiration(false);
        b.setQuantityIsZero(false);
        b.setCreatedAt(TODAY.minusDays(10));
        b.setExpirationAt(TODAY.plusDays(30));
        b.setProduct(product());
        return b;
    }

    private void assertFields(BatchRequest r, Batch b, Product p) {
        assertAll(
                () -> assertEquals(r.getBatchCode(), b.getBatchCode()),
                () -> assertEquals(r.getQuantity(), b.getQuantity()),
                () -> assertEquals(r.getExpirationAt(), b.getExpirationAt()),
                () -> assertSame(p, b.getProduct())
        );
    }
}