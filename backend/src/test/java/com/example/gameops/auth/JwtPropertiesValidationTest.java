package com.example.gameops.auth;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JwtPropertiesValidationTest {

  private static ValidatorFactory factory;
  private static Validator validator;

  @BeforeAll
  static void init() {
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @AfterAll
  static void close() {
    factory.close();
  }

  @Test
  void 빈_secret_은_검증_실패() {
    Set<ConstraintViolation<JwtProperties>> violations =
        validator.validate(new JwtProperties("", 30, 7));
    assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("secret"));
  }

  @Test
  void 너무_짧은_secret_은_검증_실패() {
    Set<ConstraintViolation<JwtProperties>> violations =
        validator.validate(new JwtProperties("short", 30, 7));
    assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("secret"));
  }

  @Test
  void _32바이트_이상_secret_은_통과() {
    Set<ConstraintViolation<JwtProperties>> violations =
        validator.validate(new JwtProperties("a".repeat(32), 30, 7));
    assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("secret"));
  }

  @Test
  void 음수_expiration_은_검증_실패() {
    Set<ConstraintViolation<JwtProperties>> violations =
        validator.validate(new JwtProperties("a".repeat(32), -1, 7));
    assertThat(violations)
        .anyMatch(v -> v.getPropertyPath().toString().equals("accessExpirationMinutes"));
  }
}
