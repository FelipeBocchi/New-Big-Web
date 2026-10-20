package com.new_big.web.controller.employee.dto;

import com.new_big.web.enums.EmployeeRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Cpf é obrigatório")
    //@CPF(message = "CPF é inválido")
    private String cpf;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Usename é obrigatório")
    private String username;

    @NotBlank(message = "Senha é obrigatório")
    private String password;

    @NotNull(message = "Um cliente deve ser ativo ou inativo")
    private Boolean active;

    @NotNull(message = "Um cliente deve ser ativo ou inativo")
    private Boolean admin;

    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

}
