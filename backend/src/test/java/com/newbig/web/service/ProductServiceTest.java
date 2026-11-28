package com.newbig.web.service;

import com.newbig.web.controller.product.dto.ProductRequest;
import com.newbig.web.entity.Product;
import com.newbig.web.enums.ProductType;
import com.newbig.web.enums.UnitType;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService — testes unitários")
class ProductServiceTest {
    private static final Long ID = 1L;

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Nested
    @DisplayName("save — cadastro")
    class Save {
        @Test
        @DisplayName("Deve mapear os dados e retornar o produto salvo")
        void deveSalvar() {
            // Arrange
            ProductRequest request = request();
            Product saved = new Product();
            saved.setId(ID);

            when(repository.existsByBarcode(request.getBarcode()))
                    .thenReturn(false);
            when(repository.save(any(Product.class))).thenReturn(saved);

            // Act
            Product result = service.save(request);

            // Assert
            ArgumentCaptor<Product> captor =
                    ArgumentCaptor.forClass(Product.class);

            verify(repository).save(captor.capture());
            assertMapped(request, captor.getValue());
            assertSame(saved, result);
        }

        @Test
        @DisplayName("Deve rejeitar código de barras duplicado")
        void deveRejeitarDuplicidade() {
            // Arrange
            ProductRequest request = request();

            when(repository.existsByBarcode(request.getBarcode()))
                    .thenReturn(true);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.save(request)
            );

            verify(repository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Deve rejeitar código de outro produto no PATCH sem salvar")
        void deveRejeitarCodigoDuplicadoNoPatch() {
            // Arrange
            Product existing = product();

            ProductRequest patch = new ProductRequest();
            patch.setBarcode("002");

            when(repository.findById(ID))
                    .thenReturn(Optional.of(existing));

            when(repository.existsByBarcode("002"))
                    .thenReturn(true);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.updatePartial(patch, ID)
            );

            assertEquals("001", existing.getBarcode());
            verify(repository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Deve aceitar o próprio código no PATCH sem verificar duplicidade")
        void deveAceitarCodigoAtualNoPatch() {
            // Arrange
            Product existing = product();

            ProductRequest patch = new ProductRequest();
            patch.setBarcode(existing.getBarcode());

            when(repository.findById(ID))
                    .thenReturn(Optional.of(existing));

            when(repository.save(existing))
                    .thenReturn(existing);

            // Act
            Product result = service.updatePartial(patch, ID);

            // Assert
            assertSame(existing, result);
            assertEquals("001", existing.getBarcode());

            verify(repository, never()).existsByBarcode(anyString());
            verify(repository).save(existing);
        }
    }

    @Nested
    @DisplayName("Consultas")
    class Queries {
        @Test
        @DisplayName("Deve retornar produto pelo ID")
        void deveBuscarPorId() {
            // Arrange
            Product product = product();
            when(repository.findById(ID))
                    .thenReturn(Optional.of(product));

            // Act
            Product result = service.findById(ID);

            // Assert
            assertSame(product, result);
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

        @Test
        @DisplayName("Deve retornar produto pelo código de barras")
        void deveBuscarPorBarcode() {
            // Arrange
            Product product = product();
            when(repository.findByBarcode("001")).thenReturn(product);

            // Act
            Product result = service.findByBarcode("001");

            // Assert
            assertSame(product, result);
        }

        @Test
        @DisplayName("Deve rejeitar código de barras inexistente")
        void deveRejeitarBarcodeAusente() {
            // Arrange
            when(repository.findByBarcode("001")).thenReturn(null);

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.findByBarcode("001")
            );
        }

        @ParameterizedTest(name = "Lista vazia: {0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("Deve retornar a listagem, inclusive vazia")
        void deveListar(boolean empty) {
            // Arrange
            List<Product> expected =
                    empty ? List.of() : List.of(product());

            when(repository.findAll()).thenReturn(expected);

            // Act
            List<Product> result = service.findAll();

            // Assert
            assertEquals(expected, result);
        }

        @ParameterizedTest(name = "Ativo: {0}; lista vazia: {1}")
        @CsvSource({
                "true,false",
                "false,false",
                "true,true",
                "false,true"
        })
        @DisplayName("Deve filtrar atividade e rejeitar ausência de resultados — contrato atual")
        void deveFiltrarAtividade(boolean active, boolean empty) {
            // Arrange
            Product product = product();
            product.setActive(active);

            when(repository.findByActive(active))
                    .thenReturn(empty ? List.of() : List.of(product));

            // Act + Assert
            if (empty) {
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.findByActive(active)
                );
            } else {
                assertEquals(
                        List.of(product),
                        service.findByActive(active)
                );
            }
        }
    }

    @Nested
    @DisplayName("update e updatePartial — alterações")
    class Updates {
        @ParameterizedTest(name = "Alterar código: {0}")
        @ValueSource(booleans = {false, true})
        @DisplayName("Deve atualizar produto mantendo código ou usando um novo livre")
        void deveAtualizar(boolean changeBarcode) {
            // Arrange
            Product product = product();
            ProductRequest request = changedRequest();

            if (changeBarcode) request.setBarcode("002");

            when(repository.findById(ID))
                    .thenReturn(Optional.of(product));

            if (changeBarcode) {
                when(repository.existsByBarcode("002"))
                        .thenReturn(false);
            }

            when(repository.save(product)).thenReturn(product);

            // Act
            Product result = service.update(request, ID);

            // Assert
            assertSame(product, result);
            assertMapped(request, product);
            verify(repository).save(product);

            if (!changeBarcode) {
                verify(repository, never())
                        .existsByBarcode(anyString());
            }
        }

        @Test
        @DisplayName("Deve rejeitar atualização com código de outro produto")
        void deveRejeitarDuplicidade() {
            // Arrange
            Product product = product();
            ProductRequest request = changedRequest();
            request.setBarcode("002");

            when(repository.findById(ID))
                    .thenReturn(Optional.of(product));
            when(repository.existsByBarcode("002"))
                    .thenReturn(true);

            // Act + Assert
            assertThrows(
                    ConflictException.class,
                    () -> service.update(request, ID)
            );

            assertEquals("001", product.getBarcode());
            verify(repository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Deve aplicar campos presentes no PATCH, incluindo false e tipos")
        void deveAplicarPatch() {
            // Arrange
            Product product = product();
            ProductRequest request = changedRequest();
            request.setBarcode("002");

            when(repository.findById(ID))
                    .thenReturn(Optional.of(product));
            when(repository.save(product)).thenReturn(product);

            // Act
            Product result = service.updatePartial(request, ID);

            // Assert
            assertSame(product, result);
            assertPatchMapped(request, product);
            verify(repository).save(product);
        }

        @Test
        @DisplayName("Deve preservar os campos omitidos no PATCH")
        void devePreservarCampos() {
            // Arrange
            Product product = product();
            ProductRequest expected = request();
            ProductRequest patch = new ProductRequest();

            when(repository.findById(ID))
                    .thenReturn(Optional.of(product));
            when(repository.save(product)).thenReturn(product);

            // Act
            service.updatePartial(patch, ID);

            // Assert
            assertMapped(expected, product);
            assertEquals(UnitType.UNIT, product.getUnitType());
            assertEquals(ProductType.BEVERAGE, product.getProductType());
            verify(repository).save(product);
        }

        @ParameterizedTest(name = "PATCH: {0}")
        @ValueSource(booleans = {false, true})
        @DisplayName("Deve rejeitar alteração de produto inexistente")
        void deveRejeitarAusente(boolean partial) {
            // Arrange
            ProductRequest request = request();
            when(repository.findById(ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                if (partial) service.updatePartial(request, ID);
                else service.update(request, ID);
            });

            verify(repository, never()).save(any(Product.class));
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
            Product product = product();
            product.setActive(before);

            when(repository.findById(ID))
                    .thenReturn(Optional.of(product));

            // Act
            service.toggle(ID);

            // Assert
            assertEquals(after, product.getActive());
            verify(repository).save(product);
        }

        @Test
        @DisplayName("Deve rejeitar toggle de produto inexistente")
        void deveRejeitarAusente() {
            // Arrange
            when(repository.findById(ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.toggle(ID)
            );

            verify(repository, never()).save(any(Product.class));
        }
    }

    private ProductRequest request() {
        ProductRequest r = new ProductRequest();
        r.setName("Suco");
        r.setDescription("Suco de uva");
        r.setCategory("Bebidas");
        r.setBarcode("001");
        r.setActive(true);
        r.setMinimumStock(3);
        r.setCostPrice(new BigDecimal("5.00"));
        r.setSalePrice(new BigDecimal("8.00"));
        r.setUnitType(UnitType.UNIT);
        r.setProductType(ProductType.BEVERAGE);
        return r;
    }

    private ProductRequest changedRequest() {
        ProductRequest r = request();
        r.setName("Sorvete");
        r.setDescription("Sorvete de chocolate");
        r.setCategory("Gelados");
        r.setActive(false);
        r.setMinimumStock(7);
        r.setCostPrice(new BigDecimal("9.00"));
        r.setSalePrice(new BigDecimal("15.00"));
        r.setUnitType(UnitType.KG);
        r.setProductType(ProductType.ICE_CREAM);
        return r;
    }

    private Product product() {
        ProductRequest r = request();
        Product p = new Product();
        p.setId(ID);
        p.setName(r.getName());
        p.setDescription(r.getDescription());
        p.setCategory(r.getCategory());
        p.setBarcode(r.getBarcode());
        p.setActive(r.getActive());
        p.setMinimumStock(r.getMinimumStock());
        p.setCostPrice(r.getCostPrice());
        p.setSalePrice(r.getSalePrice());
        p.setUnitType(r.getUnitType());
        p.setProductType(r.getProductType());
        return p;
    }

    private void assertMapped(ProductRequest r, Product p) {
        assertCommonFields(r, p);
        assertEquals(r.getMinimumStock(), p.getMinimumStock());

        // unitType/productType ausentes no save/update:
        // lacuna discutida separadamente.
    }

    private void assertPatchMapped(ProductRequest r, Product p) {
        assertCommonFields(r, p);
        assertEquals(r.getUnitType(), p.getUnitType());
        assertEquals(r.getProductType(), p.getProductType());

        // minimumStock ausente no PATCH:
        // lacuna discutida separadamente.
    }

    private void assertCommonFields(ProductRequest r, Product p) {
        assertAll(
                () -> assertEquals(r.getName(), p.getName()),
                () -> assertEquals(r.getDescription(), p.getDescription()),
                () -> assertEquals(r.getCategory(), p.getCategory()),
                () -> assertEquals(r.getBarcode(), p.getBarcode()),
                () -> assertEquals(r.getActive(), p.getActive()),
                () -> assertEquals(
                        0,
                        r.getCostPrice().compareTo(p.getCostPrice())
                ),
                () -> assertEquals(
                        0,
                        r.getSalePrice().compareTo(p.getSalePrice())
                )
        );
    }
}