package com.new_big.web.repository;

import com.new_big.web.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Boolean existsByBarcode(String barcode);

    Product findByBarcode(String barcode);

    List<Product> findByActive(Boolean active);
}