package com.example.gameops.common.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.gameops.common.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new DummyController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void BusinessException_은_ErrorCode_상태와_표준_응답을_반환한다() throws Exception {
    mockMvc
        .perform(get("/dummy/business"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.data").doesNotExist())
        .andExpect(jsonPath("$.error.code").value(ErrorCode.NOT_FOUND.getCode()))
        .andExpect(jsonPath("$.error.message").value(ErrorCode.NOT_FOUND.getMessage()));
  }

  @Test
  void 검증_실패_시_400과_field_에러_메시지를_반환한다() throws Exception {
    mockMvc
        .perform(post("/dummy/validate").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_REQUEST.getCode()))
        .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.containsString("name")));
  }

  @Test
  void 알수없는_예외는_500과_INTERNAL_ERROR로_반환한다() throws Exception {
    mockMvc
        .perform(get("/dummy/unknown"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error.code").value(ErrorCode.INTERNAL_ERROR.getCode()));
  }

  @RestController
  static class DummyController {

    @GetMapping("/dummy/business")
    void business() {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }

    @PostMapping("/dummy/validate")
    void validate(@Valid @RequestBody DummyRequest req) {}

    @GetMapping("/dummy/unknown")
    void unknown() {
      throw new IllegalStateException("boom");
    }
  }

  record DummyRequest(@NotBlank String name) {}
}
