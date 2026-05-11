package com.example.gameops.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    @NotBlank(message = "app.jwt.secret (env JWT_SECRET) must be provided.")
        @Size(min = 32, message = "app.jwt.secret must be at least 32 bytes for HS256.")
        String secret,
    @Positive long accessExpirationMinutes,
    @Positive long refreshExpirationDays) {}
