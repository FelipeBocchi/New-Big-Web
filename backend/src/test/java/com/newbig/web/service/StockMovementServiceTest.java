package com.newbig.web.service;

import com.newbig.web.controller.batch.dto.BatchConsumption;
import com.newbig.web.entity.*;
import com.newbig.web.enums.MovementType;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.BatchRepository;
import com.newbig.web.repository.EmployeeRepository;
import com.newbig.web.repository.StockMovementRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMovementService — testes unitários")
class StockMovementServiceTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 12, 10, 0);

    @Mock
    private StockMovementRepository repository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private StockMovementService service;

    @Captor
    private ArgumentCaptor<StockMovement> movementsCaptor;

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
    @DisplayName("saveMovementSale — movimentação de saída por venda")
    class SaveMovementSale {

        @Test
        @DisplayName("Deve criar e salvar uma movimentação para cada lote consumido")
        void deveCriarMovimentosPorLote() {
            // Arrange
            Sale sale = sale();
            Batch firstBatch = batch(10L);
            Batch secondBatch = batch(20L);

            when(batchRepository.findById(10L))
                    .thenReturn(Optional.of(firstBatch));

            when(batchRepository.findById(20L))
                    .thenReturn(Optional.of(secondBatch));

            // Act
            List<StockMovement> result =
                    service.saveMovementSale(consumptions(), sale);

            // Assert
            verify(repository, times(2))
                    .save(movementsCaptor.capture());

            List<StockMovement> saved =
                    movementsCaptor.getAllValues();

            assertEquals(2, result.size());

            assertSame(result.get(0), saved.get(0));
            assertSame(result.get(1), saved.get(1));

            assertMovement(result.get(0), firstBatch, sale, 2);
            assertMovement(result.get(1), secondBatch, sale, 5);

            assertNotSame(result.get(0), result.get(1));
        }

        @Test
        @DisplayName("Deve retornar lista vazia sem consultar ou salvar quando não houver consumo")
        void deveAceitarConsumoVazio() {
            // Arrange
            Sale sale = sale();

            // Act
            List<StockMovement> result =
                    service.saveMovementSale(List.of(), sale);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(batchRepository, repository);
        }

        @ParameterizedTest(name = "Lote inexistente na posição: {0}")
        @ValueSource(ints = {0, 1})
        @DisplayName("Deve interromper a operação sem salvar quando um lote não existir")
        void deveRejeitarLoteAusente(int failedIndex) {
            // Arrange
            Sale sale = sale();

            ResourceNotFoundException failure =
                    new ResourceNotFoundException("Lote ausente");

            if (failedIndex == 1) {
                when(batchRepository.findById(10L))
                        .thenReturn(Optional.of(batch(10L)));
            }

            Long failedId = failedIndex == 0 ? 10L : 20L;

            when(batchRepository.findById(failedId))
                    .thenThrow(failure);

            // Act + Assert
            ResourceNotFoundException result = assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.saveMovementSale(consumptions(), sale)
            );

            assertSame(failure, result);
            verifyNoInteractions(repository);
        }

        @ParameterizedTest(name = "Falha na gravação de posição: {0}")
        @ValueSource(ints = {0, 1})
        @DisplayName("Deve propagar falha de gravação e interromper as chamadas seguintes")
        void devePropagarFalhaAoSalvar(int failedIndex) {
            // Arrange
            Sale sale = sale();

            when(batchRepository.findById(10L))
                    .thenReturn(Optional.of(batch(10L)));

            when(batchRepository.findById(20L))
                    .thenReturn(Optional.of(batch(20L)));

            IllegalStateException failure =
                    new IllegalStateException("Falha de gravação");

            if (failedIndex == 0) {
                when(repository.save(any(StockMovement.class)))
                        .thenThrow(failure);
            } else {
                when(repository.save(any(StockMovement.class)))
                        .thenAnswer(returnsFirstArg())
                        .thenThrow(failure);
            }

            // Act + Assert
            IllegalStateException result = assertThrows(
                    IllegalStateException.class,
                    () -> service.saveMovementSale(consumptions(), sale)
            );

            assertSame(failure, result);

            verify(repository, times(failedIndex + 1))
                    .save(any(StockMovement.class));

            // Verifica chamadas do mock; não comprova rollback no banco.
        }

        @Test
        @DisplayName("Deve registrar entrada sem dobrar a quantidade do lote")
        void deveRegistrarEntrada() {
            // Arrange
            Batch batch = batch(10L);
            batch.setBatchCode("L-A");
            batch.setQuantity(7);

            Employee employee = new Employee();
            employee.setId(3L);

            when(employeeRepository.findById(3L))
                    .thenReturn(Optional.of(employee));

            when(repository.save(any(StockMovement.class)))
                    .thenAnswer(returnsFirstArg());

            // Act
            StockMovement result =
                    service.saveMovementEntry(batch, 3L);

            // Assert
            assertAll(
                    () -> assertEquals(MovementType.ENTRY, result.getType()),
                    () -> assertEquals(7, result.getQuantity()),
                    () -> assertEquals(7, batch.getQuantity()),
                    () -> assertSame(batch, result.getBatch()),
                    () -> assertSame(employee, result.getEmployee()),
                    () -> assertNull(result.getSale()),
                    () -> assertEquals(NOW, result.getMovementDate())
            );

            verify(repository).save(result);
            verifyNoInteractions(batchRepository);
        }

        @Test
        @DisplayName("Deve rejeitar entrada com funcionário inexistente")
        void deveRejeitarResponsavelAusente() {
            // Arrange
            Batch batch = batch(10L);
            batch.setQuantity(7);

            when(employeeRepository.findById(3L))
                    .thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.saveMovementEntry(batch, 3L)
            );

            verifyNoInteractions(repository);
        }
    }

    @ParameterizedTest(name = "Quantidade inválida: {0}")
    @ValueSource(ints = {0, -1})
    @DisplayName("Deve validar todos os consumos antes de consultar ou gravar")
    void deveRejeitarConsumoNaoPositivo(int quantity) {
        // Arrange — primeiro consumo válido, segundo inválido.
        List<BatchConsumption> consumptions = List.of(
                new BatchConsumption(10L, "L-A", 2),
                new BatchConsumption(20L, "L-B", quantity)
        );

        Sale sale = sale();

        // Act + Assert
        assertThrows(
                ConflictException.class,
                () -> service.saveMovementSale(consumptions, sale)
        );

        verifyNoInteractions(
                repository,
                batchRepository,
                employeeRepository
        );
    }

    private List<BatchConsumption> consumptions() {
        return List.of(
                new BatchConsumption(10L, "L-A", 2),
                new BatchConsumption(20L, "L-B", 5)
        );
    }

    private Sale sale() {
        Employee employee = new Employee();
        employee.setId(3L);

        Sale sale = new Sale();
        sale.setId(1L);
        sale.setEmployee(employee);

        return sale;
    }

    private Batch batch(Long id) {
        Batch batch = new Batch();
        batch.setId(id);
        return batch;
    }

    private void assertMovement(
            StockMovement movement,
            Batch batch,
            Sale sale,
            int quantity
    ) {
        assertAll(
                () -> assertEquals(
                        "Movimentação vinda da venda Id: 1",
                        movement.getObservation()
                ),
                () -> assertEquals(quantity, movement.getQuantity()),
                () -> assertEquals(NOW, movement.getMovementDate()),
                () -> assertEquals(
                        MovementType.SALE_EXIT,
                        movement.getType()
                ),
                () -> assertSame(batch, movement.getBatch()),
                () -> assertSame(
                        sale.getEmployee(),
                        movement.getEmployee()
                ),
                () -> assertSame(sale, movement.getSale())
        );
    }
}