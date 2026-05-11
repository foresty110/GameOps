package com.example.gameops.auth;

import com.example.gameops.admin.AdminRepository;
import com.example.gameops.admin.domain.Admin;
import com.example.gameops.audit.AuditLogRepository;
import com.example.gameops.audit.domain.AuditLog;
import com.example.gameops.auth.dto.LoginRequest;
import com.example.gameops.auth.dto.LoginResponse;
import com.example.gameops.common.error.ErrorCode;
import com.example.gameops.common.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final AdminRepository adminRepository;
  private final AuditLogRepository auditLogRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public AuthService(
      AdminRepository adminRepository,
      AuditLogRepository auditLogRepository,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider) {
    this.adminRepository = adminRepository;
    this.auditLogRepository = auditLogRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Transactional
  public LoginResponse login(LoginRequest request, String ipAddress) {
    Admin admin =
        adminRepository
            .findByUsername(request.username())
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS));

    if (!admin.isActive()
        || !passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
      throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS);
    }

    String accessToken = jwtTokenProvider.createAccessToken(admin);
    String refreshToken = jwtTokenProvider.createRefreshToken(admin);

    auditLogRepository.save(
        AuditLog.builder()
            .adminId(admin.getId())
            .actionType("LOGIN")
            .targetType("ADMIN")
            .targetId(String.valueOf(admin.getId()))
            .ipAddress(ipAddress)
            .build());

    return new LoginResponse(accessToken, refreshToken, admin.getRole(), admin.getDisplayName());
  }
}
