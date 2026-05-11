package com.example.gameops.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret, long accessExpirationMinutes, long refreshExpirationDays) {}
