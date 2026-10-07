
package com.new_big.web.controller.product;

import com.new_big.web.controller.employee.dto.EmployeeRequest;
import com.new_big.web.controller.employee.dto.EmployeeResponse;
import com.new_big.web.controller.product.dto.ProductRequest;
import com.new_big.web.controller.product.dto.ProductResponse;
import com.new_big.web.entity.Employee;
import com.new_big.web.entity.Product;
import com.new_big.web.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@CrossOrigin("*")
public class ProductController {

    private final ProductService service;
    public ProductController(ProductService service) {
        this.service = service;
    }

    // CREATED
    // POST LOCALHOST:8080/API/PRODUCT
    @PostMapping()
    public ResponseEntity<ProductResponse> save(
            @Valid @RequestBody ProductRequest request
    ) {

        Product product = this.service.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponse.de(product));
    }

    // FIND_BY_ID
    // GET LOCALHOST:8080/API/PRODUCT/2
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @PathVariable Long id
    ) {

        Product product = this.service.findById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ProductResponse.de(product));
    }

    // FIND_BY_BARCODE
    // GET LOCALHOST:8080/API/PRODUCT/barcode/001
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ProductResponse> findByBarcode(
            @PathVariable String barcode
    ) {

        Product product = this.service.findByBarcode(barcode);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ProductResponse.de(product));
    }

    // FIND_BY_ACTIVE
    // GET LOCALHOST:8080/API/PRODUCT/ACTIVE/TRUE
    @GetMapping("/active/{active}")
    public ResponseEntity<List<ProductResponse>> findByActive(
            @PathVariable Boolean active
    ) {

        List<ProductResponse> response = this.service.findByActive(active).stream()
                .map(ProductResponse::de)
                .toList();

        return ResponseEntity.ok(response);

    }

    // FIND_ALL
    // GET LOCALHOST:8080/API/PRODUCT
    @GetMapping()
    public ResponseEntity<List<ProductResponse>> findAll() {

        List<ProductResponse> response = this.service.findAll().stream()
                .map(ProductResponse::de)
                .toList();

        return ResponseEntity.ok(response);
    }

    // UPDATE
    // PUT LOCALHOST:8080/API/PRODUCT/6
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {

        Product product = this.service.update(request, id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponse.de(product));
    }

    // UPDATE_PARTIAL
    // PATCH LOCALHOST:8080/API/PRODUCT/7
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updatePartial(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {

        Product product = this.service.updatePartial(request, id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponse.de(product));
    }

    // INATIVA / ATIVA O STATUS DO PRODUTO
    // DELETE LOCALHOST:8080/API/PRODUCT/5
    @DeleteMapping("/{id}")
    public ResponseEntity<?> toggleStatus(
            @PathVariable Long id
    ) {

        service.toggle(id);

        return ResponseEntity
                .noContent()
                .build();
    }


}

