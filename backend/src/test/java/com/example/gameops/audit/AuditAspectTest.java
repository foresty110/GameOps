package com.example.gameops.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.gameops.audit.domain.AuditLog;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(AuditAspectTest.TestConfig.class)
class AuditAspectTest {

  @Autowired DummyService dummyService;
  @Autowired AuditLogRepository auditLogRepository;

  @BeforeEach
  void clear() {
    auditLogRepository.deleteAll();
    SecurityContextHolder.clearContext();
  }

  @Test
  void Auditable_메서드_호출_시_AuditLog_가_저장된다() {
    dummyService.doSomething("test-reason");

    List<AuditLog> logs = auditLogRepository.findAll();
    assertThat(logs).hasSize(1);
    AuditLog log = logs.get(0);
    assertThat(log.getActionType()).isEqualTo("TEST_ACTION");
    assertThat(log.getTargetType()).isEqualTo("TEST_TARGET");
    assertThat(log.getReason()).isEqualTo("test-reason");
    assertThat(log.getAdminId()).isEqualTo(AuditAspect.SYSTEM_ADMIN_ID);
    assertThat(log.getCreatedAt()).isNotNull();
  }

  @Test
  void 메서드가_예외를_던지면_AuditLog_는_저장되지_않는다() {
    try {
      dummyService.failing();
    } catch (RuntimeException ignored) {
    }

    assertThat(auditLogRepository.findAll()).isEmpty();
  }

  @TestConfiguration
  static class TestConfig {

    @Bean
    DummyService dummyService() {
      return new DummyService();
    }
  }

  static class DummyService {

    @Auditable(actionType = "TEST_ACTION", targetType = "TEST_TARGET", reasonParam = "reason")
    public void doSomething(String reason) {}

    @Auditable(actionType = "TEST_FAIL", targetType = "TEST_TARGET")
    public void failing() {
      throw new IllegalStateException("boom");
    }
  }
}
