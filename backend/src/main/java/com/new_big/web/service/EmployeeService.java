package com.new_big.web.service;

import com.new_big.web.controller.customer.dto.CustomerRequest;
import com.new_big.web.controller.employee.dto.EmployeeRequest;
import com.new_big.web.entity.Customer;
import com.new_big.web.entity.Employee;
import com.new_big.web.enums.EmployeeRole;
import com.new_big.web.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;

    @Transactional
    public Employee save(EmployeeRequest request) {

        if (repository.existsByCpf(request.getCpf())) throw new RuntimeException("CPF já cadastrado");
        if (repository.existsByEmail(request.getEmail())) throw new RuntimeException("E-mail já cadastrado");

        Employee employee = new Employee();
        updateFields(employee, request);
        //employee.setCreatedAt(LocalDateTime.now());
        return repository.save(employee);
    }

    public Employee findById( Long id) {

        return repository.findById(id).orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }


    public List<Employee> findAll() {
        return this.repository.findAll();
    }


    public Employee update(EmployeeRequest request, Long id) {

        Employee employee = findById(id);
        if (!employee.getCpf().equals(request.getCpf()) && repository.existsByCpf(request.getCpf()))
            throw new RuntimeException("CPF já pertence a outro funcionário");

        updateFields(employee, request);
        return repository.save(employee);

    }

    public Employee updatePartial(EmployeeRequest employeeRequest, Long id) {

        Employee employee = this.findById(id);

        if (employeeRequest.getName() != null) employee.setName(employeeRequest.getName());
        if (employeeRequest.getCpf() != null) employee.setCpf(employeeRequest.getCpf());
        if (employeeRequest.getPhone() != null) employee.setPhone(employeeRequest.getPhone());
        if (employeeRequest.getEmail() != null) employee.setEmail(employeeRequest.getEmail());
        if (employeeRequest.getUsername() != null) employee.setUsername(employeeRequest.getUsername());
        if (employeeRequest.getPassword() != null) employee.setPassword(employeeRequest.getPassword());
        if (employeeRequest.getActive() != null) employee.setActive(employeeRequest.getActive());
        if (employeeRequest.getAdmin() != null) employee.setAdmin(employeeRequest.getAdmin());
        if (employeeRequest.getBirthDate() != null) employee.setBirthDate(employeeRequest.getBirthDate());
        if (employeeRequest.getRole() != null) employee.setRole(employeeRequest.getRole());

        return this.repository.save(employee);

    }

    @Transactional
    public void inactivate(Long id) {
        Employee employee = findById(id);
        employee.setActive(false);
        repository.save(employee);
    }

    private void updateFields(Employee employee, EmployeeRequest request) {
        employee.setName(request.getName());
        employee.setCpf(request.getCpf());
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setPassword(request.getPassword()); // Lembrete: Encriptar com BCrypt no futuro
        employee.setActive(request.getActive());
        employee.setRole(request.getRole());
        employee.setAdmin(request.getAdmin());
        //employee.setBirthDate(request.getBirthDate().atStartOfDay());
    }


    public List<Employee> findByRoleAndActive(EmployeeRole role, Boolean active) {

        return this.repository.findByRoleAndActive(role, active);

    }

}
