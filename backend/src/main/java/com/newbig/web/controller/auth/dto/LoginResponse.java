package com.newbig.web.controller.auth.dto;

public record LoginResponse(
        String token,
        String tokenType,
        Long expiresIn
) {
}
