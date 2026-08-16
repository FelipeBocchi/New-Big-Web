package com.new_big.web.controller.employee.dto;

import com.new_big.web.enums.EmployeeRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class EmployeeRequest {

    private String name;
    private String cpf;
    private String phone;
    private String email;
    private String username;
    private String password;
    private Boolean active;
    private Boolean admin;
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

}
