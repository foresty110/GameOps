package com.example.gameops.seed;

import com.example.gameops.admin.AdminRepository;
import com.example.gameops.admin.domain.Admin;
import com.example.gameops.admin.domain.AdminStatus;
import com.example.gameops.admin.domain.Role;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("local")
public class LocalSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(LocalSeeder.class);
  private static final String DEFAULT_PASSWORD = "Admin1234!";

  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;

  public LocalSeeder(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
    this.adminRepository = adminRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedAdmins();
  }

  private void seedAdmins() {
    if (adminRepository.count() > 0) {
      log.info("LocalSeeder: admins already present, skipping admin seed.");
      return;
    }
    String hash = passwordEncoder.encode(DEFAULT_PASSWORD);
    adminRepository.saveAll(
        List.of(
            buildAdmin("super", hash, Role.SUPER_ADMIN, "최고관리자"),
            buildAdmin("gm1", hash, Role.GM, "GM 김운영"),
            buildAdmin("cs1", hash, Role.CS, "CS 박응대"),
            buildAdmin("viewer1", hash, Role.VIEWER, "VIEWER 이조회")));
    log.info("LocalSeeder: inserted 4 admin accounts (password: {}).", DEFAULT_PASSWORD);
  }

  private static Admin buildAdmin(String username, String hash, Role role, String displayName) {
    return Admin.builder()
        .username(username)
        .passwordHash(hash)
        .role(role)
        .displayName(displayName)
        .status(AdminStatus.ACTIVE)
        .build();
  }
}
