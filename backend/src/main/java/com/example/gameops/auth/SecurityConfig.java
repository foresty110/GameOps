package com.example.gameops.auth;

import com.example.gameops.common.error.ErrorCode;
import com.example.gameops.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ObjectMapper objectMapper;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/login")
                    .permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            eh ->
                eh.authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  private AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) -> writeError(response, ErrorCode.UNAUTHORIZED);
  }

  private AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> writeError(response, ErrorCode.FORBIDDEN);
  }

  private void writeError(HttpServletResponse response, ErrorCode code)
      throws java.io.IOException {
    response.setStatus(code.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response
        .getWriter()
        .write(objectMapper.writeValueAsString(ApiResponse.fail(code.getCode(), code.getMessage())));
  }
}
