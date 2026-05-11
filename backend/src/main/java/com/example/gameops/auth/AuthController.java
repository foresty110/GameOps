package com.example.gameops.auth;

import com.example.gameops.auth.dto.LoginRequest;
import com.example.gameops.auth.dto.LoginResponse;
import com.example.gameops.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    return ApiResponse.ok(authService.login(request, resolveIp(httpRequest)));
  }

  @GetMapping("/me")
  public ApiResponse<AdminPrincipal> me(@AuthenticationPrincipal AdminPrincipal principal) {
    return ApiResponse.ok(principal);
  }

  private String resolveIp(HttpServletRequest req) {
    String forwarded = req.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return req.getRemoteAddr();
  }
}
