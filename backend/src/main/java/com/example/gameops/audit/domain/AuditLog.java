package com.example.gameops.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "admin_id", nullable = false)
  private Long adminId;

  @Column(name = "action_type", nullable = false, length = 64)
  private String actionType;

  @Column(name = "target_type", nullable = false, length = 64)
  private String targetType;

  @Column(name = "target_id", length = 128)
  private String targetId;

  @Column(name = "before_json", columnDefinition = "TEXT")
  private String beforeJson;

  @Column(name = "after_json", columnDefinition = "TEXT")
  private String afterJson;

  @Column(length = 500)
  private String reason;

  @Column(name = "ip_address", length = 64)
  private String ipAddress;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Builder
  private AuditLog(
      Long adminId,
      String actionType,
      String targetType,
      String targetId,
      String beforeJson,
      String afterJson,
      String reason,
      String ipAddress) {
    this.adminId = adminId;
    this.actionType = actionType;
    this.targetType = targetType;
    this.targetId = targetId;
    this.beforeJson = beforeJson;
    this.afterJson = afterJson;
    this.reason = reason;
    this.ipAddress = ipAddress;
    this.createdAt = LocalDateTime.now();
  }
}
