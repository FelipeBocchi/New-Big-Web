package com.new_big.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.new_big.web.controller.product.ProductController;
import com.new_big.web.controller.product.dto.ProductRequest;
import com.new_big.web.entity.Product;
import com.new_big.web.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("Deve criar um produto com sucesso e retornar status 201 Created")
    void deveCriarProdutoComSucesso() throws Exception {
        // Arrange (Dado)
        ProductRequest request = new ProductRequest();
        request.setName("Casquinha");
        request.setDescription("Casquinha crocante");
        request.setCategory("Sorvetes");
        request.setBarcode("123"); // Máximo de 3 caracteres conforme o @Size
        request.setMinimumStock(10);
        request.setActive(true);
        request.setSalePrice(BigDecimal.valueOf(5.00));
        request.setCostPrice(BigDecimal.valueOf(2.00));

        Product productMock = new Product();
        productMock.setId(1L);
        productMock.setName(request.getName());
        productMock.setBarcode(request.getBarcode());
        productMock.setSalePrice(request.getSalePrice());
        productMock.setActive(request.getActive());

        when(productService.save(any(ProductRequest.class))).thenReturn(productMock);

        // Act & Assert (Quando & Então)
        mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Casquinha"))
                .andExpect(jsonPath("$.barcode").value("123"));
    }

    @Test
    @DisplayName("Deve buscar um produto por ID com sucesso e retornar status 200 OK")
    void deveBuscarProdutoPorIdComSucesso() throws Exception {
        // Arrange (Dado)
        Long productId = 1L;
        Product productMock = new Product();
        productMock.setId(productId);
        productMock.setName("Casquinha");
        productMock.setBarcode("123");

        when(productService.findById(productId)).thenReturn(productMock);

        // Act & Assert (Quando & Então)
        mockMvc.perform(get("/api/product/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Casquinha"))
                .andExpect(jsonPath("$.barcode").value("123"));
    }

    @Test
    @DisplayName("Deve listar todos os produtos com sucesso e retornar status 200 OK")
    void deveListarTodosOsProdutosComSucesso() throws Exception {
        // Arrange (Dado)
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Casquinha");
        p1.setBarcode("123");

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Cascão");
        p2.setBarcode("456");

        List<Product> products = List.of(p1, p2);

        when(productService.findAll()).thenReturn(products);

        // Act & Assert (Quando & Então)
        mockMvc.perform(get("/api/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Casquinha"))
                .andExpect(jsonPath("$[1].name").value("Cascão"));
    }

    @Test
    @DisplayName("Deve alterar o status (ativar/inativar) do produto com sucesso e retornar status 204 No Content")
    void deveAlternarStatusDoProdutoComSucesso() throws Exception {
        // Arrange (Dado)
        Long productId = 1L;
        doNothing().when(productService).toggle(productId);

        // Act & Assert (Quando & Então)
        mockMvc.perform(delete("/api/product/{id}", productId))
                .andExpect(status().isNoContent());
    }
}