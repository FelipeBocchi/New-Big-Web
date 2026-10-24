package com.new_big.web.controller.employee;

import com.new_big.web.controller.employee.dto.EmployeeRequest;
import com.new_big.web.controller.employee.dto.EmployeeResponse;
import com.new_big.web.entity.Employee;
import com.new_big.web.enums.EmployeeRole;
import com.new_big.web.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin("*")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController( EmployeeService service) { this.service = service; }

    // CREATED
    // POST LOCALHOST:8080/API/EMPLOYEE
    @PostMapping()
    public ResponseEntity<EmployeeResponse> save(
            @Valid @RequestBody EmployeeRequest request
    ) {

        Employee employee = this.service.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EmployeeResponse.de(employee));
    }

    // FIND_BY_ID
    // GET LOCALHOST:8080/API/EMPLOYEE/2
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> findById(
            @PathVariable Long id
    ) {

        Employee employee = this.service.findById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(EmployeeResponse.de(employee));
    }

    // FIND_ALL
    // GET LOCALHOST:8080/API/EMPLOYEE
    @GetMapping()
    public ResponseEntity<List<EmployeeResponse>> findAll() {

        List<EmployeeResponse> response = this.service.findAll().stream()
                .map(EmployeeResponse::de)
                .toList();

        return ResponseEntity.ok(response);
    }

    // UPDATE
    // PUT LOCALHOST:8080/API/EMPLOYEE/6
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {

        Employee employee = this.service.update(request, id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EmployeeResponse.de(employee));
    }

    // UPDATE_PARTIAL
    // PATCH LOCALHOST:8080/API/EMPLOYEE/7
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updatePartial(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {

        Employee employee = this.service.updatePartial(request, id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EmployeeResponse.de(employee));
    }

    // INATIVA / ATIVA O STATUS DO FUNCIONÁRIOS
    // DELETE LOCALHOST:8080/API/EMPLOYEE/5
    @DeleteMapping("/{id}")
    public ResponseEntity<?> toggleStatus(
            @PathVariable Long id
    ) {

        service.toggle(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    //  === FUNÇÕES FORA O CRUD BÁSICO ===

    // LISTA FUNCIONÁRIOS PELO ACTIVE (TRUE OR FALSE)
    // GET LOCALHOST:8080/API/EMPLOYEE/TRUE
   @GetMapping("/active/{active}")
    public ResponseEntity<List<EmployeeResponse>> findByActive(
            @PathVariable Boolean active
    ) {

        List<EmployeeResponse> responseList = this.service.findByActive(active).stream()
                .map(EmployeeResponse::de)
                .toList();
        return ResponseEntity.ok(responseList);
    }


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
