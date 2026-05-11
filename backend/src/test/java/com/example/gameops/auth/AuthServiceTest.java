package com.example.gameops.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.gameops.admin.AdminRepository;
import com.example.gameops.admin.domain.Admin;
import com.example.gameops.admin.domain.AdminStatus;
import com.example.gameops.admin.domain.Role;
import com.example.gameops.audit.AuditLogRepository;
import com.example.gameops.audit.domain.AuditLog;
import com.example.gameops.auth.dto.LoginRequest;
import com.example.gameops.auth.dto.LoginResponse;
import com.example.gameops.common.error.ErrorCode;
import com.example.gameops.common.exception.BusinessException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock AdminRepository adminRepository;
  @Mock AuditLogRepository auditLogRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtTokenProvider jwtTokenProvider;

  @InjectMocks AuthService authService;

  private Admin admin;

  @BeforeEach
  void setUp() {
    admin =
        Admin.builder()
            .username("gm1")
            .passwordHash("hashed")
            .role(Role.GM)
            .displayName("GM One")
            .status(AdminStatus.ACTIVE)
            .build();
    ReflectionTestUtils.setField(admin, "id", 42L);
  }

  @Test
  void 정상_자격증명이면_토큰을_발급하고_AuditLog_를_저장한다() {
    given(adminRepository.findByUsername("gm1")).willReturn(Optional.of(admin));
    given(passwordEncoder.matches("pw", "hashed")).willReturn(true);
    given(jwtTokenProvider.createAccessToken(admin)).willReturn("ACCESS");
    given(jwtTokenProvider.createRefreshToken(admin)).willReturn("REFRESH");

    LoginResponse response = authService.login(new LoginRequest("gm1", "pw"), "1.2.3.4");

    assertThat(response.accessToken()).isEqualTo("ACCESS");
    assertThat(response.refreshToken()).isEqualTo("REFRESH");
    assertThat(response.role()).isEqualTo(Role.GM);
    assertThat(response.displayName()).isEqualTo("GM One");
    verify(auditLogRepository).save(any(AuditLog.class));
  }

  @Test
  void 존재하지_않는_사용자면_AUTH_BAD_CREDENTIALS() {
    given(adminRepository.findByUsername("ghost")).willReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest("ghost", "pw"), "1.2.3.4"))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_BAD_CREDENTIALS);
    verify(auditLogRepository, never()).save(any());
  }

  @Test
  void 비밀번호_불일치면_AUTH_BAD_CREDENTIALS() {
    given(adminRepository.findByUsername("gm1")).willReturn(Optional.of(admin));
    given(passwordEncoder.matches("wrong", "hashed")).willReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest("gm1", "wrong"), "1.2.3.4"))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_BAD_CREDENTIALS);
    verify(auditLogRepository, never()).save(any());
  }

  @Test
  void 비활성_계정이면_AUTH_BAD_CREDENTIALS() {
    Admin inactive =
        Admin.builder()
            .username("gm1")
            .passwordHash("hashed")
            .role(Role.GM)
            .displayName("GM One")
            .status(AdminStatus.INACTIVE)
            .build();
    given(adminRepository.findByUsername("gm1")).willReturn(Optional.of(inactive));

    assertThatThrownBy(() -> authService.login(new LoginRequest("gm1", "pw"), "1.2.3.4"))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_BAD_CREDENTIALS);
  }
}
