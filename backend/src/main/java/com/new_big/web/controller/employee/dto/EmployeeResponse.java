package com.new_big.web.controller.employee.dto;

import com.new_big.web.entity.Employee;
import com.new_big.web.enums.EmployeeRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;
import java.time.LocalDateTime;

//  = Não coloquei o atributo admim, se for necessário futuramente

public record EmployeeResponse(
         String name,
         String cpf,
         String phone,
         String email,
         String username,
         Boolean active,
         LocalDateTime createAt,
         LocalDate birthDate,

        @Enumerated(EnumType.STRING)
         EmployeeRole role
) {

    public static EmployeeResponse de(Employee employee) {
            return new EmployeeResponse(
                    employee.getName(),
                    employee.getCpf(),
                    employee.getPhone(),
                    employee.getEmail(),
                    employee.getUsername(),
                    employee.getActive(),
                    employee.getCreateAt(),
                    employee.getBirthDate(),
                    employee.getRole()
            );
    }
}
