package com.new_big.web.controller.employee;

import com.new_big.web.controller.employee.dto.EmployeeRequest;
import com.new_big.web.controller.employee.dto.EmployeeResponse;
import com.new_big.web.entity.Employee;
import com.new_big.web.enums.EmployeeRole;
import com.new_big.web.service.EmployeeService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    // POST LOCALHOST:8080/API/EMPLOYEE
    @PostMapping()
    public ResponseEntity<EmployeeResponse> save( @RequestBody EmployeeRequest employeeRequest) {

        try {

           Employee employee = this.service.save(employeeRequest);
           return new ResponseEntity<EmployeeResponse>( EmployeeResponse.de(employee), HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // GET LOCALHOST:8080/API/EMPLOYEE/2
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById ( @PathVariable Long id) {

        try {

            Employee employee = this.service.findById(id);
            return new ResponseEntity<EmployeeResponse>( EmployeeResponse.de(employee), HttpStatus.CREATED);

        } catch (ResponseStatusException responseStatusException) {
            throw responseStatusException;
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // GET LOCALHOST:8080/API/EMPLOYEE
    @GetMapping()
    public ResponseEntity<List<EmployeeResponse>> findAll() {

        try {

            List<EmployeeResponse> employee = this.service.list()
                    .stream()
                    .map(EmployeeResponse::de)
                    .toList();

            return new ResponseEntity<>( employee, HttpStatus.ACCEPTED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // PUT LOCALHOST:8080/API/EMPLOYEE/6
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update( @PathVariable Long id,
                                                    @RequestBody EmployeeRequest employeeRequest
    ) {
        try {

            Employee employee = this.service.update(employeeRequest, id);
            return new ResponseEntity<EmployeeResponse>( EmployeeResponse.de(employee), HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // PATCH LOCALHOST:8080/API/EMPLOYEE/7
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updatePartial( @PathVariable Long id,
                                                           @RequestBody EmployeeRequest employeeRequest
    ) {
        try {

            Employee employee = this.service.updatePartial(employeeRequest, id);
            return new ResponseEntity<EmployeeResponse>( EmployeeResponse.de(employee), HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete( @PathVariable Long id) {

        try {

            this.service.delete(id);
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    //  === FUNÇÕES FORA O CRUD BÁSICO ===

    //  = Função para filtar funcionário de uma determinada função e se estçao etivos
    // GET LOCALHOST:8080/API/EMPLOYEE/FILTAR?role=caixa&active=true
    @GetMapping("/filtar")
    public ResponseEntity<List<EmployeeResponse>> findByRoleAndActive(
            @RequestParam EmployeeRole employeeRole,
            @RequestParam Boolean active
            ) {

        try {

            List<EmployeeResponse> employeeList = this.service.findByRoleAndActive(employeeRole, active)
                    .stream()
                    .map(EmployeeResponse::de)
                    .toList();

            return new ResponseEntity<>(employeeList, HttpStatus.ACCEPTED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

}
