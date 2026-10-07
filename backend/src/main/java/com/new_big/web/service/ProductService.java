package com.new_big.web.service;

import com.new_big.web.controller.product.dto.ProductRequest;
import com.new_big.web.entity.Employee;
import com.new_big.web.entity.Product;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    @Transactional
    public Product save(ProductRequest request) {

        log.info("Criando novo funcionário. name={}", request.getName());

        if (this.repository.existsByBarcode(request.getBarcode())) {
            log.warn("Cadastro rejeitado: Barcode já existente ({})", request.getBarcode());
            throw new ConflictException("Já existe um produto cadastrado com este barcode");
        }

        Product product = new Product();
        updateFields(product, request);

        Product saved = this.repository.save(product);
        log.info("Produto criado com sucesso. name={}", saved.getName());
        return saved;
    }

    public Product findById(Long id) {

        return this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: \" + id"));
    }

    public Product findByBarcode(String barcode) {

        Product product = this.repository.findByBarcode(barcode);

        if (product == null) { throw new ResourceNotFoundException("Produto não encontrado com o Barcode: \" + barcode");}

        return product;
    }

    public List<Product> findByActive( Boolean active) {

        List<Product> list = repository.findByActive(active);

        if (list.isEmpty()) throw new ResourceNotFoundException(  "Nenhum produto encontrado com active: " + active );

        return list;
    }

    public List<Product> findAll() { return this.repository.findAll(); }

    @Transactional
    public Product update(ProductRequest request, Long id) {
        log.info("Atualizando produto. id={}", id);
        Product product = findById(id);

        if (!product.getBarcode().equals(request.getBarcode()) && repository.existsByBarcode(request.getBarcode())) {
            throw new ConflictException("Já existe outro produto cadastrado com este Barcode");
        }

        updateFields(product, request);
        log.info("Produto atualizado com sucesso. id={}", id);
        return repository.save(product);
    }

    @Transactional
    public Product updatePartial(ProductRequest request, Long id) {
        log.info("Atualizando produto. id={}", id);
        Product product = this.findById(id);

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getBarcode() != null) product.setBarcode(request.getBarcode());
        if (request.getActive() != null) product.setActive(request.getActive());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getSalePrice() != null) product.setSalePrice(request.getSalePrice());
        if (request.getProductType() != null) product.setProductType(request.getProductType());
        if (request.getUnitType() != null) product.setUnitType(request.getUnitType());

        return this.repository.save(product);
    }

    @Transactional
    public void toggle(Long id) {

        Product product = this.findById(id);

        if (product.getActive()) {
            log.info("Inativando produto. id={}", id);
            product.setActive(false);
        } else {
            log.info("Ativando produto. id={}", id);
            product.setActive(true);
        }

        this.repository.save(product);
    }

    public void updateFields(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setBarcode(request.getBarcode());
        product.setActive(request.getActive());
        product.setMinimumStock(request.getMinimumStock());
        product.setCostPrice(request.getCostPrice());
        product.setSalePrice(request.getSalePrice());
    }

}