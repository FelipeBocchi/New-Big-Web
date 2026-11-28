package com.newbig.web.controller.sale;

import com.newbig.web.config.infra.security.EmployeeUserDetails;
import com.newbig.web.controller.sale.dto.*;
import com.newbig.web.entity.Sale;
import com.newbig.web.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sale")
@CrossOrigin("http://localhost:8081")
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

    @GetMapping("/{id}")
    public SaleDetailsResponse findById(@PathVariable Long id) {
        return service.findDetails(id);
    }

    @PostMapping("/{id}/cancel")
    public SaleCloseResponse cancel(
            @PathVariable Long id,
            @Valid @RequestBody SaleCancelRequest request,
            @AuthenticationPrincipal EmployeeUserDetails employee
    ) {
        return SaleCloseResponse.de(service.cancel(id, request.cashId(), employee.getEmployeeId()));
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


}
