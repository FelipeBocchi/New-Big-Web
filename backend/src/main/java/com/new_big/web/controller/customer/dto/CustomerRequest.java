package com.new_big.web.controller.customer.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Cpf é obrigatório")
    @CPF(message = "CPF é inválido")
    private String cpf;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotNull(message = "Um cliente deve ser ativo ou inativo")
    private Boolean active;

    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate birthDate;

}
