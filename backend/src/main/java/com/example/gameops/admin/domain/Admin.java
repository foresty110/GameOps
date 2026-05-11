package com.example.gameops.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Role role;

  @Column(name = "display_name", nullable = false, length = 64)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private AdminStatus status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Builder
  private Admin(
      String username,
      String passwordHash,
      Role role,
      String displayName,
      AdminStatus status) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
    this.displayName = displayName;
    this.status = status;
    this.createdAt = LocalDateTime.now();
  }

  public boolean isActive() {
    return status == AdminStatus.ACTIVE;
  }
}
