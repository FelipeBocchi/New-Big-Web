package com.newbig.web.config.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.newbig.web.entity.Employee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final String issuer;

    public JwtService(
            @Value("${api.security.token.secret:minha-secret-default}") String secret,
            @Value("${api.security.token.issuer:minha-aplicacao}") String issuer) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
    }

    public String gerarToken(Employee employee) {
        try {
            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(employee.getEmail())
                    .withClaim("id", employee.getId())
                    .withClaim("role", employee.getAdmin()) // dar uma olhada pq estou usando o boolean admin e não o role
                    .withExpiresAt(Instant.now().plus(8, ChronoUnit.HOURS))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new IllegalStateException("Erro ao gerar token JWT", exception);
        }
    }

    public String validarToken(String token) {
        try {
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}
