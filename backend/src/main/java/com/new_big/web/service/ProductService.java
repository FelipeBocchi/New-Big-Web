package com.new_big.web.service;

import com.new_big.web.entity.Product;
import com.new_big.web.controller.product.dto.ProductRequestDTO;
import com.new_big.web.controller.product.dto.ProductResponseDTO;
import com.new_big.web.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public List<ProductResponseDTO> getAllProducts() {
        return this.repository.findAll().stream()
                .map(ProductResponseDTO::new)
                .toList();
    }

    public void createProduct(ProductRequestDTO data) {
        Product newProduct = new Product(data);
        this.repository.save(newProduct);
    }

    public Optional<Product> updateProduct(String id, ProductRequestDTO data) {
        Optional<Product> optionalProduct = this.repository.findById(id);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(data.name());
            product.setSalePrice(data.salePrice());
            this.repository.save(product);
            return Optional.of(product);
        }

        return Optional.empty();
    }

    public boolean deleteProduct(String id) {
        Optional<Product> optionalProduct = this.repository.findById(id);

        if (optionalProduct.isPresent()) {
            this.repository.delete(optionalProduct.get());
            return true;
        }

        return false;
    }
}