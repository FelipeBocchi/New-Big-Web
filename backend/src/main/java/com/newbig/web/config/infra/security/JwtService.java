package com.newbig.web.config.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final String issuer;
    private final long expirationSeconds;

    public JwtService(
            @Value("${api.security.token.secret}") String secret,
            @Value("${api.security.token.issuer:new-big-web}") String issuer,
            @Value("${api.security.token.expiration-seconds:900}") long expirationSeconds) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("A chave JWT precisa ter pelo menos 32 bytes");
        }
        if (issuer.isBlank() || expirationSeconds <= 0) {
            throw new IllegalArgumentException("Emissor e validade do JWT são obrigatórios");
        }
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String gerarToken(EmployeeUserDetails employee) {
        try {
            Instant now = Instant.now();

            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(employee.getUsername())
                    .withClaim("id", employee.getEmployeeId())
                    .withArrayClaim(
                            "authorities",
                            employee.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .toArray(String[]::new)
                    )
                    .withIssuedAt(now)
                    .withExpiresAt(now.plusSeconds(expirationSeconds))
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
