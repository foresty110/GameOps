package com.example.gameops.common.error;

import com.example.gameops.common.exception.BusinessException;
import com.example.gameops.common.response.ApiResponse;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
    ErrorCode code = ex.getErrorCode();
    return ResponseEntity.status(code.getStatus())
        .body(ApiResponse.fail(code.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus())
        .body(ApiResponse.fail(ErrorCode.INVALID_REQUEST.getCode(), message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
        .body(
            ApiResponse.fail(
                ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage()));
  }
}
