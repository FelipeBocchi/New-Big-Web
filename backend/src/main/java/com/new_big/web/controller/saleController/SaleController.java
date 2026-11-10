package com.new_big.web.controller.saleController;

import com.new_big.web.controller.saleController.dto.*;
import com.new_big.web.entity.Sale;
import com.new_big.web.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sale")
@CrossOrigin("*")
public class SaleController {

    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    // OPEN
    // POST LOCALHOST:8080/API/SALE
    @PostMapping()
    public ResponseEntity<SaleOpenResponse> open(
            @Valid @RequestBody SaleOpenRequest request
    ) {

        Sale sale = this.service.open(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SaleOpenResponse.de(sale));
    }

    // CLOSE
    // PATCH LOCALHOST:8080/API/SALE
    @PatchMapping("/{id}")
    public ResponseEntity<SaleCloseResponse> close(
            @Valid @RequestBody SaleCloseRequest request,
            @PathVariable Long id
    ) {

        Sale sale = this.service.close(request, id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SaleCloseResponse.de(sale));
    }

    // FIND_ALL
    // GET LOCALHOST:8080/API/SALE
    @GetMapping()
    public ResponseEntity<List<SaleCloseResponse>> findAll() {

        List<SaleCloseResponse> saleList = this.service.findAll().stream()
                .map(SaleCloseResponse::de)
                .toList();

        return ResponseEntity.ok(saleList);
    }

    // ADD_ITEMS
    // PUT LOCALHOST:8080/API/SALE
    @PutMapping()
    public ResponseEntity<String> addItems(
            @Valid @RequestBody SaleAddRequest request
    ) {

        String messenger = this.service.addItems(request);

        return ResponseEntity.ok(messenger);
    }



    /*// REMOVE_ITEMS
    // DELETE LOCALHOST:8080/API/SALE/5
    //  !!! vou ter que pensar
    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeItems(
            @PathVariable Long id
    ) {

    }
    */

}
