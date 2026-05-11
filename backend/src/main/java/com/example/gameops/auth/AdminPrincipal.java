package com.example.gameops.auth;

import com.example.gameops.admin.domain.Role;

public record AdminPrincipal(Long id, String username, Role role, String displayName) {}
