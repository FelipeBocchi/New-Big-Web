package com.new_big.web.service;

import com.new_big.web.controller.product.dto.ProductRequest;
import com.new_big.web.entity.Product;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Deve salvar um produto com sucesso quando o código de barras não estiver cadastrado")
    void deveSalvarProdutoComSucesso() {
        // Arrange (Dado)
        ProductRequest request = new ProductRequest();
        request.setName("Coca-Cola 2L");
        request.setBarcode("7891000");
        request.setCostPrice(BigDecimal.valueOf(8.0));
        request.setSalePrice(BigDecimal.valueOf(12.0));
        request.setActive(true);

        when(repository.existsByBarcode(anyString())).thenReturn(false);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (Quando)
        Product result = productService.save(request);

        // Assert (Então)
        assertNotNull(result);
        assertEquals("Coca-Cola 2L", result.getName());
        assertEquals("7891000", result.getBarcode());
        verify(repository, times(1)).existsByBarcode("7891000");
        verify(repository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar ConflictException ao tentar salvar produto com código de barras já existente (Caminho Triste)")
    void naoDeveSalvarProdutoComBarcodeDuplicado() {
        // Arrange (Dado)
        ProductRequest request = new ProductRequest();
        request.setBarcode("7891000");

        when(repository.existsByBarcode("7891000")).thenReturn(true);

        // Act & Assert (Quando & Então)
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            productService.save(request);
        });

        assertEquals("Já existe um produto cadastrado com este barcode", exception.getMessage());
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve retornar um produto com sucesso ao buscar por ID existente")
    void deveBuscarProdutoPorIdComSucesso() {
        // Arrange (Dado)
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Refrigerante");

        when(repository.findById(productId)).thenReturn(Optional.of(product));

        // Act (Quando)
        Product result = productService.findById(productId);

        // Assert (Então)
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Refrigerante", result.getName());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar por ID inexistente (Caminho Triste)")
    void naoDeveEncontrarProdutoPorIdInexistente() {
        // Arrange (Dado)
        Long productId = 99L;
        when(repository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert (Quando & Então)
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.findById(productId);
        });

        assertTrue(exception.getMessage().contains("Produto não encontrado com o ID"));
    }

    @Test
    @DisplayName("Deve inativar ou ativar o status do produto com sucesso (Toggle)")
    void deveAlternarStatusDoProduto() {
        // Arrange (Dado)
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setActive(true);

        when(repository.findById(productId)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenReturn(product);

        // Act (Quando)
        productService.toggle(productId);

        // Assert (Então)
        assertFalse(product.getActive()); // Como estava true, deve ter mudado para false
        verify(repository, times(1)).save(product);
    }
}