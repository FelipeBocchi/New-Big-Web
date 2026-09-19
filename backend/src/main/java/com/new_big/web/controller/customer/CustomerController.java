package com.new_big.web.controller.customer;

import com.new_big.web.controller.customer.dto.CustomerRequest;
import com.new_big.web.controller.customer.dto.CustomerResponse;
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

        try {
            Customer customer = this.service.save(customerRequest);
            return new ResponseEntity<CustomerResponse>( CustomerResponse.de(customer), HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // FIND_BY_ID
    // GET LOCALHOST:8080/API/CUSTOMER/5
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {

        try {
            Customer customer = this.service.findById(id);
            return new ResponseEntity<CustomerResponse>( CustomerResponse.de(customer), HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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

        try {
            Customer customer = service.update(id, request);
            return ResponseEntity.ok(CustomerResponse.de(customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // INATIVA
    // DELETE LOCALHOST:8080/API/CUSTOMER/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            service.inactivate(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
