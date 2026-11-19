package com.newbig.web.controller.auth;

import com.newbig.web.controller.auth.dto.LoginRequest;
import com.newbig.web.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import javax.naming.ConfigurationException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public String login(
            @Valid @RequestBody LoginRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }

        return service.login(request);
    }
}
