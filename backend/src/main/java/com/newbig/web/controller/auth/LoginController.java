package com.newbig.web.controller.auth;

import com.newbig.web.controller.auth.dto.LoginRequest;
import com.newbig.web.controller.auth.dto.LoginResponse;
import com.newbig.web.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return service.login(request);
    }
}
