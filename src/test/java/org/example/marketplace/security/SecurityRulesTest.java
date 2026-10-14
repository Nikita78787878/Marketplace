package org.example.marketplace.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
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
  void deleteProduct_withKeycloakAdminClaims_returns204() throws Exception {
    mockMvc.perform(delete("/api/v1/products/1")
            .with(keycloakJwt("ADMIN")))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteProduct_withKeycloakUserClaims_returns403() throws Exception {
    mockMvc.perform(delete("/api/v1/products/1")
            .with(keycloakJwt("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteProduct_withoutRealmAccessClaim_returns403() throws Exception {
    mockMvc.perform(delete("/api/v1/products/1")
            .with(jwt().authorities(new KeycloakRealmRoleConverter())))
        .andExpect(status().isForbidden());
  }

  /**
   * Токен в форме Keycloak: роли лежат в realm_access.roles.
   * authorities(...) обязателен — без него jwt() берёт права из "scope"
   * своим дефолтным конвертером и наш KeycloakRealmRoleConverter не вызывается.
   */
  private static JwtRequestPostProcessor keycloakJwt(String... roles) {
    return jwt()
        .jwt(j -> j.claim("realm_access", Map.of("roles", List.of(roles))))
        .authorities(new KeycloakRealmRoleConverter());
  }

  @Test
  void accessProtectedEndpoint_withGarbageToken_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/products")
            .header("Authorization", "Bearer не.токен.вовсе"))
        .andExpect(status().isUnauthorized());
  }
}