package com.new_big.web.controller;

import com.new_big.web.entity.Product;
import com.new_big.web.controller.product.dto.ProductRequestDTO;
import com.new_big.web.controller.product.dto.ProductResponseDTO;
import com.new_big.web.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity postProduct(@RequestBody @Valid ProductRequestDTO body) {
        this.productService.createProduct(body);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> productList = this.productService.getAllProducts();
        return ResponseEntity.ok(productList);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateProduct(@PathVariable String id, @RequestBody @Valid ProductRequestDTO body) {
        Optional<Product> updatedProduct = this.productService.updateProduct(id, body);

        if (updatedProduct.isPresent()) {
            return ResponseEntity.ok(new ProductResponseDTO(updatedProduct.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteProduct(@PathVariable String id) {
        boolean isDeleted = this.productService.deleteProduct(id);

        if (isDeleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}