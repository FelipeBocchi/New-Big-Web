package com.newbig.web.controller.auth.dto;

import com.newbig.web.config.validation.CompleteRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(
            groups = CompleteRequest.class, message = "Nome é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    @Email
    private String email;

    @NotBlank(
            groups = CompleteRequest.class, message = "Nome é obrigatório"
    )
    @Pattern(
            regexp = "(?s).*\\S.*", message = "O campo não pode estar em branco"
    )
    private String password;
}
