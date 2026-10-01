package org.example.marketplace.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional // ← каждый тест откатывается
class SecurityRulesTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp(WebApplicationContext context) {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
  }

  @Test
  @WithAnonymousUser
  void getProducts_withoutAuth_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/products"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void getProducts_asUser_returns200() throws Exception {
    mockMvc.perform(get("/api/v1/products"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "USER")
  void deleteProduct_asUser_returns403() throws Exception {
    mockMvc.perform(delete("/api/v1/products/1"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteProduct_asAdmin_isNotForbidden() throws Exception {
    mockMvc.perform(delete("/api/v1/products/1"))
        .andExpect(status().isNoContent());
  }

  @Test
  void login_thenAccessProtectedEndpoint_withRealToken() throws Exception {
    // 1. логинимся — permitAll, токен не нужен
    String response = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String token = JsonPath.read(response, "$.token");

    // 2. идём с этим токеном на ручку, требующую ROLE_ADMIN
    mockMvc.perform(delete("/api/v1/products/1")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
  }

  @Test
  void accessProtectedEndpoint_withGarbageToken_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/products")
            .header("Authorization", "Bearer не.токен.вовсе"))
        .andExpect(status().isUnauthorized());
  }
}