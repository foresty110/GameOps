package com.example.gameops.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.gameops.admin.AdminRepository;
import com.example.gameops.admin.domain.Admin;
import com.example.gameops.admin.domain.AdminStatus;
import com.example.gameops.admin.domain.Role;
import com.example.gameops.common.error.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired AdminRepository adminRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    adminRepository.deleteAll();
    adminRepository.save(
        Admin.builder()
            .username("gm1")
            .passwordHash(passwordEncoder.encode("Admin1234!"))
            .role(Role.GM)
            .displayName("GM One")
            .status(AdminStatus.ACTIVE)
            .build());
  }

  @Test
  void 정상_로그인은_200과_토큰을_반환한다() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"gm1\",\"password\":\"Admin1234!\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
        .andExpect(jsonPath("$.data.role").value("GM"))
        .andExpect(jsonPath("$.data.displayName").value("GM One"));
  }

  @Test
  void 잘못된_자격증명은_401과_표준_에러_응답() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"gm1\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value(ErrorCode.AUTH_BAD_CREDENTIALS.getCode()));
  }

  @Test
  void 토큰_없이_보호_엔드포인트_호출시_401() throws Exception {
    mockMvc
        .perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value(ErrorCode.UNAUTHORIZED.getCode()));
  }

  @Test
  void 정상_토큰으로_me_엔드포인트_조회는_200() throws Exception {
    MvcResult login =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"gm1\",\"password\":\"Admin1234!\"}"))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode root = objectMapper.readTree(login.getResponse().getContentAsString());
    String accessToken = root.path("data").path("accessToken").asText();

    mockMvc
        .perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("gm1"))
        .andExpect(jsonPath("$.data.role").value("GM"));
  }

  @Test
  void 검증_실패는_400() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_REQUEST.getCode()));
  }
}
