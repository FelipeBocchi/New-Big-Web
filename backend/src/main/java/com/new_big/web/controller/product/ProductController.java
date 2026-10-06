
package com.new_big.web.controller.product;

import com.new_big.web.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
@CrossOrigin("*")
public class ProductController {

    private final ProductService service;
    public ProductController(ProductService service) {
        this.service = service;
    }

    // CREATED
    // POST LOCALHOST:8080/API/EMPLOYEE
    public ResponseEntity<ProductResponse> save() {

    }

}

