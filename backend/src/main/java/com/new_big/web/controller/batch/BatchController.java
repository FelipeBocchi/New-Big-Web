package com.new_big.web.controller.batch;

import com.new_big.web.controller.batch.dto.BatchRequest;
import com.new_big.web.controller.batch.dto.BatchResponse;
import com.new_big.web.controller.product.dto.ProductRequest;
import com.new_big.web.controller.product.dto.ProductResponse;
import com.new_big.web.entity.Batch;
import com.new_big.web.entity.Product;
import com.new_big.web.service.BatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batch")
@CrossOrigin("*")
public class BatchController {

    private final BatchService service;

    public BatchController(BatchService service) {
        this.service = service;
    }

    // CREATED
    // POST LOCALHOST:8080/API/BATCH
    @PostMapping()
    public ResponseEntity<BatchResponse> save(
            @Valid @RequestBody BatchRequest request
    ) {

        Batch batch = this.service.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BatchResponse.de(batch));
    }

    // FIND_BY_ID
    // GET LOCALHOST:8080/API/BATCH/2
    @GetMapping("/{id}")
    public ResponseEntity<BatchResponse> findById(
            @PathVariable Long id
    ) {

        Batch batch = this.service.findById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BatchResponse.de(batch));
    }

    // FIND_BY_ID
    // GET LOCALHOST:8080/API/EXPIRATION/BATCH/TRUE
    @GetMapping("/expiration/{expiration}")
    public ResponseEntity<List<BatchResponse>> findByExpiration(
            @PathVariable Boolean expiration
    ) {

        List<BatchResponse> response = this.service.findByExpiration(expiration).stream()
                .map(BatchResponse::de)
                .toList();

        return ResponseEntity.ok(response);
    }

    // FIND_ALL
    // GET LOCALHOST:8080/API/BATCH
    @GetMapping()
    public ResponseEntity<List<BatchResponse>> findAll() {

        List<BatchResponse> response = this.service.findAll().stream()
                .map(BatchResponse::de)
                .toList();

        return ResponseEntity.ok(response);
    }

    // UPDATE
    // PUT LOCALHOST:8080/API/BATCH/6
    @PutMapping("/{id}")
    public ResponseEntity<BatchResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BatchRequest request
    ) {

        Batch batch = this.service.update(request, id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BatchResponse.de(batch));
    }

    // UPDATE_PARTIAL
    // PATCH LOCALHOST:8080/API/BATCH/7
    @PatchMapping("/{id}")
    public ResponseEntity<BatchResponse> updatePartial(
            @PathVariable Long id,
            @Valid @RequestBody BatchRequest request
    ) {

        Batch batch = this.service.updatePartial(request, id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BatchResponse.de(batch));
    }


}
