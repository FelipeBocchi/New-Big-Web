package com.new_big.web.repository;

import com.new_big.web.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {/*

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve retornar verdadeiro ao verificar existência de produto por código de barras já cadastrado")
    void deveVerificarSeExistePorBarcodeComSucesso() {
        // Arrange (Dado)
        Product product = new Product();
        product.setName("Sorvete de Chocolate");
        product.setBarcode("789123456");
        product.setCostPrice(BigDecimal.valueOf(15.0));
        product.setSalePrice(BigDecimal.valueOf(25.0));
        product.setActive(true);

        entityManager.persistAndFlush(product);

        // Act (Quando)
        boolean exists = productRepository.existsByBarcode("789123456");

        // Assert (Então)
        assertTrue(exists);
    }

    @Test
    @DisplayName("Deve buscar o produto corretamente pelo código de barras")
    void deveBuscarPorBarcodeComSucesso() {
        // Arrange (Dado)
        Product product = new Product();
        product.setName("Açaí 1L");
        product.setBarcode("987654321");
        product.setCostPrice(BigDecimal.valueOf(20.0));
        product.setSalePrice(BigDecimal.valueOf(35.0));
        product.setActive(true);

        entityManager.persistAndFlush(product);

        // Act (Quando)
        Product found = productRepository.findByBarcode("987654321");

        // Assert (Então)
        assertNotNull(found);
        assertEquals("Açaí 1L", found.getName());
        assertEquals("987654321", found.getBarcode());
    }

    @Test
    @DisplayName("Deve listar os produtos filtrando pelo status ativo")
    void deveListarPorStatusAtivo() {
        // Arrange (Dado)
        Product activeProduct = new Product();
        activeProduct.setName("Produto Ativo");
        activeProduct.setBarcode("111");
        activeProduct.setCostPrice(BigDecimal.valueOf(5.0));
        activeProduct.setSalePrice(BigDecimal.valueOf(10.0));
        activeProduct.setActive(true);

        Product inactiveProduct = new Product();
        inactiveProduct.setName("Produto Inativo");
        inactiveProduct.setBarcode("222");
        inactiveProduct.setCostPrice(BigDecimal.valueOf(5.0));
        inactiveProduct.setSalePrice(BigDecimal.valueOf(10.0));
        inactiveProduct.setActive(false);

        entityManager.persist(activeProduct);
        entityManager.persist(inactiveProduct);
        entityManager.flush();

        // Act (Quando)
        List<Product> activeList = productRepository.findByActive(true);

        // Assert (Então)
        assertFalse(activeList.isEmpty());
        assertTrue(activeList.stream().allMatch(Product::getActive));
    }
 */}