package com.example.gameops.auth.dto;

import com.example.gameops.admin.domain.Role;

public record LoginResponse(
    String accessToken, String refreshToken, Role role, String displayName) {}
