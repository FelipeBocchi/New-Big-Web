package com.newbig.web.service;

import com.newbig.web.config.infra.security.EmployeeUserDetails;
import com.newbig.web.config.infra.security.JwtService;
import com.newbig.web.controller.auth.dto.LoginRequest;
import com.newbig.web.controller.auth.dto.LoginResponse;
import org.springframework.security.authentication.BadCredentialsException;
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

    public LoginResponse login(LoginRequest data) {
        if (data == null || data.getEmail() == null || data.getEmail().isBlank()
                || data.getPassword() == null || data.getPassword().isBlank()) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword());
        var authentication = authenticationManager.authenticate(usernamePassword);
        var usuario = (EmployeeUserDetails) authentication.getPrincipal();

        return new LoginResponse(jwtService.gerarToken(usuario), "Bearer", jwtService.getExpirationSeconds());
    }
}
