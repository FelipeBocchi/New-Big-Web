package com.newbig.web.service;

import com.newbig.web.config.infra.security.JwtService;
import com.newbig.web.controller.auth.dto.LoginRequest;
import com.newbig.web.entity.Employee;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest data) {
        if (data.getEmail().isBlank() || data.getPassword().isBlank()) {
            throw new IllegalArgumentException("Login e senha não podem ser vazios");
        }
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword());
        var authentication = authenticationManager.authenticate(usernamePassword);
        var usuario = (Employee) authentication.getPrincipal();

        return jwtService.gerarToken(usuario);
    }
}
