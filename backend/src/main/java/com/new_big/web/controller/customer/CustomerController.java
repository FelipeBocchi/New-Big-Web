package com.new_big.web.controller.customer;

import com.new_big.web.controller.customer.dto.CustomerRequest;
import com.new_big.web.controller.customer.dto.CustomerResponse;
import com.new_big.web.controller.employee.dto.EmployeeResponse;
import com.new_big.web.entity.Customer;
import com.new_big.web.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/customer")
@CrossOrigin("*")
public class CustomerController {

    private final CustomerService service;

    public CustomerController( CustomerService service) {
        this.service = service;
    }

    // CREATE
    // POST LOCALHOST:8080/API/CUSTOMER
    @PostMapping()
    public ResponseEntity<CustomerResponse> save(@Valid @RequestBody CustomerRequest customerRequest) {

        Customer customer = this.service.save(customerRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomerResponse.de(customer));
    }

    // FIND_BY_ID
    // GET LOCALHOST:8080/API/CUSTOMER/5
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {

        Customer customer = this.service.findById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(CustomerResponse.de(customer));
    }

    // FIND_ALL
    // GET LOCALHOST:8080/API/CUSTOMER
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll() {
        List<CustomerResponse> responses = service.findAll().stream()
                .map(CustomerResponse::de)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // UPDATE
    // PUT LOCALHOST:8080/API/CUSTOMER/5
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {

        Customer customer = this.service.update(id, request);

        return ResponseEntity
                .ok(CustomerResponse.de(customer));
    }

    // INATIVA / ATIVA
    // DELETE LOCALHOST:8080/API/CUSTOMER/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        this.service.inactivate(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    // LISTA CLIENTE PELO ACTIVE (TRUE OR FALSE)
    // GET LOCALHOST:8080/API/CUSTOMER/TRUE
    @GetMapping("/active/{active}")
    public ResponseEntity<List<CustomerResponse>> findByActive(
            @PathVariable Boolean active
    ) {

        List<CustomerResponse> responseList = this.service.findByActive(active).stream()
                .map(CustomerResponse::de)
                .toList();
        return ResponseEntity.ok(responseList);
    }

}
