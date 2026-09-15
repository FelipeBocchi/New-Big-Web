package com.new_big.web.service;

import com.new_big.web.controller.employee.dto.EmployeeRequest;
import com.new_big.web.entity.Employee;
import com.new_big.web.enums.EmployeeRole;
import com.new_big.web.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    public Employee save(EmployeeRequest employeeRequest) {

        Employee employee = new Employee();

        employee.setName(employeeRequest.getName());
        employee.setCpf(employeeRequest.getCpf());
        employee.setPhone(employeeRequest.getPhone());
        employee.setEmail(employeeRequest.getEmail());
        employee.setUsername(employeeRequest.getUsername());
        employee.setPassword(employeeRequest.getPassword());
        employee.setActive(employeeRequest.getActive());
        employee.setAdmin(employeeRequest.getAdmin());
        employee.setBirthDate(employeeRequest.getBirthDate());
        employee.setRole(employeeRequest.getRole());

        return this.repository.save(employee);

    }

    public Employee findById( Long id) {

        return this.repository.findById(id).
                orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Funcionário não foi encontrado com o id" + id)
                );

    }


    public List<Employee> list() {
        return this.repository.findAll();
    }


    public Employee update(EmployeeRequest employeeRequest, Long id) {

        Employee employee = this.findById(id);

        employee.setName(employeeRequest.getName());
        employee.setCpf(employeeRequest.getCpf());
        employee.setPhone(employeeRequest.getPhone());
        employee.setEmail(employeeRequest.getEmail());
        employee.setUsername(employeeRequest.getUsername());
        employee.setPassword(employeeRequest.getPassword());
        employee.setActive(employeeRequest.getActive());
        employee.setAdmin(employeeRequest.getAdmin());
        employee.setBirthDate(employeeRequest.getBirthDate());
        employee.setRole(employeeRequest.getRole());

        return this.repository.save(employee);

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


    public void delete(Long id) {

        Employee employee = this.findById(id);
        this.repository.delete(employee);

    }


    public List<Employee> findByRoleAndActive(EmployeeRole role, Boolean active) {

        return this.repository.findByRoleAndActive(role, active);

    }

}
