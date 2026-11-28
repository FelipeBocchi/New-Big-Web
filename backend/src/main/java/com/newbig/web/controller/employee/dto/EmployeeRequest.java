package com.newbig.web.controller.employee.dto;

import com.newbig.web.config.validation.CompleteRequest;
import com.newbig.web.enums.EmployeeRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(
            groups = CompleteRequest.class, message = "Nome é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    private String name;

    @NotBlank(
            groups = CompleteRequest.class, message = "CPF é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    //@CPF(message = "CPF é inválido")
    private String cpf;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    @NotBlank(
            groups = CompleteRequest.class, message = "Email é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(
            groups = CompleteRequest.class, message = "Username é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    private String username;

    @NotBlank(
            groups = CompleteRequest.class, message = "Password é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    private String password;

    @NotNull(groups = CompleteRequest.class)
    private Boolean active;

    @NotNull(groups = CompleteRequest.class)
    private Boolean admin;

    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

}
