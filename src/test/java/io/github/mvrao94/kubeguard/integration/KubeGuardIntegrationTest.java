package io.github.mvrao94.kubeguard.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mvrao94.kubeguard.KubeguardApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Integration tests for the entire KubeGuard application */
@SpringBootTest(classes = KubeguardApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class KubeGuardIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser
  void testHealthEndpoint() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  @WithMockUser
  void testSwaggerUI() throws Exception {
    mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void testApiDocs() throws Exception {
    mockMvc
        .perform(get("/api-docs"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @WithMockUser
  void testGetReports() throws Exception {
    mockMvc
        .perform(get("/api/v1/reports"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @WithMockUser
  void testGetSecurityMetrics() throws Exception {
    mockMvc
        .perform(get("/api/v1/reports/analytics/summary"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalReports").isNumber())
        .andExpect(jsonPath("$.completedReports").isNumber());
  }
}
