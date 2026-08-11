package com.new_big.web.entity;

import com.new_big.web.enums.EmployeeRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String cpf;

    private String phone;

    private String email;

    private String password;

    private Boolean active;

    private Boolean admin;

    private LocalDateTime createAt;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

}
