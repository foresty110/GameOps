package com.example.gameops.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  INVALID_REQUEST("E400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED("E401", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
  FORBIDDEN("E403", "권한이 없습니다.", HttpStatus.FORBIDDEN),
  NOT_FOUND("E404", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INTERNAL_ERROR("E500", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  AUTH_BAD_CREDENTIALS(
      "E1001", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);

  private final String code;
  private final String message;
  private final HttpStatus status;

  ErrorCode(String code, String message, HttpStatus status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
