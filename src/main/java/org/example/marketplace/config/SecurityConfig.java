package org.example.marketplace.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.example.marketplace.exception.ErrorResponse;
import org.example.marketplace.security.KeycloakRealmRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * HTTP-запрос принимает встроенный Tomcat. Внутри него две вещи:
     *
     *   Tomcat
     *    ├── Filter'ы   ← сюда Spring Security встраивает себя (один фильтр)
     *    └── Servlet    ← DispatcherServlet, он один, дальше уже контроллеры
     *
     * Фильтры отрабатывают ДО сервлета. Поэтому если security не пропустила
     * запрос — контроллер не вызовется вообще.
     *
     * Здесь мы описываем правила доступа к ручкам. HttpSecurity — билдер:
     * мы не создаём фильтры руками, а описываем желаемое, а http.build()
     * собирает из этого готовую цепочку фильтров.
     *
     * Порядок правил: срабатывает ПЕРВОЕ подходящее по пути и методу, дальше
     * список не просматривается. Поэтому если два правила могут поймать один
     * и тот же запрос — более узкое ставим выше. anyRequest() пересекается со
     * всем, поэтому всегда последний.
     *
     * anyRequest().denyAll() — принцип "запрещено всё, кроме разрешённого":
     * забытая ручка перестанет работать, а не окажется открыта наружу.
     *
     * Это является "справочником" для фильтра авторизации
     *
     *
     * по поводу строчки .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
     *
     * запрос
     *   │
     *   ▼
     * BearerTokenAuthenticationFilter
     *   │
     *   ├─ есть заголовок Authorization: Bearer ...?
     *   │      нет  -> пропускаем дальше, ничего не делаем
     *   │      да   -> берём строку токена
     *   │                │
     *   │                ▼
     *   │           jwtDecoder.decode(token)     <- твой бин, Spring найдёт сам
     *   │                │
     *   │                ├─ JwtException -> 401, до контроллера не доходит
     *   │                │
     *   │                ▼
     *   │           Jwt (claims)
     *   │                │
     *   │                ▼
     *   │           JwtAuthenticationConverter   <- дефолтный, шаг 4 его заменим
     *   │                │
     *   │                ▼
     *   │           Authentication в SecurityContextHolder
     *   ▼
     * дальше по цепочке -> правила доступа -> контроллер
     *
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
        AuthenticationEntryPoint entryPoint,
        AccessDeniedHandler deniedHandler){

        http.authorizeHttpRequests(auth -> auth
                    // Swagger открыт всем — ЭТО ТОЛЬКО ДЛЯ DEV.
                    // В проде схема API анонимно видна быть не должна:
                    // отключать профилем или закрывать hasRole("ADMIN").
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**").permitAll() // доступ для всех
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").authenticated() // залогиненнный
                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN") // только админу
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                .anyRequest().denyAll() // все остальное запрещено
                )
            // убрали т.к. это вариант входа по логину и паролю (вторая дверь).httpBasic(Customizer.withDefaults())// смотрит на "Authorization: Basic ..." -   Включает HTTP Basic Authentication — самый простой стандартный способ представиться.
                // Логин и пароль склеиваются в логин:пароль, кодируются в Base64 и едут в заголовке каждого запроса
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler)) //  смотрит на "Authorization: Bearer ..." - Название опять врёт — OAuth2 мы не используем. По факту она добавляет в цепочку BearerTokenAuthenticationFilter
            // CSRF-защита не нужна: аутентификация идёт по заголовку Authorization: Bearer,
            // который браузер НЕ подставляет автоматически (в отличие от кук).
            // Запрос с чужого сайта придёт без заголовка -> 401.
            // ВАЖНО: если токен переедет в HttpOnly-куку — CSRF вернётся,
            // и csrf.disable() придётся убирать.
            .csrf(AbstractHttpConfigurer::disable)

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler))

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // делаем явно чтобы не хранить состояние на сервере (STATEFUL), т.к. по умолчанию отдаеться куки

        return http.build();
    }





    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }


    // ---- Ошибки, которые рождаются В ФИЛЬТРАХ ----
    // Фильтры отрабатывают ДО DispatcherServlet, поэтому @RestControllerAdvice
    // их не видит. Без этих двух бинов Security отдаёт голый статус с пустым телом.
    //
    //   AuthenticationEntryPoint -> 401, "не знаю кто ты"
    //   AccessDeniedHandler      -> 403, "знаю кто ты, но нельзя"
    //
    // Сравни: неверный пароль на /auth/login летит из сервиса, то есть НИЖЕ
    // сервлета — его ловит GlobalExceptionHandler. Три обработчика, один формат.

    /**
     * не знаю, кто ты: токена нет, протух, подпись битая
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (req, res, ex) -> writeError(res, objectMapper, 401, "Требуется действительный токен");
    }

    /**
     * знаю, кто ты, но нельзя: роли не хватает
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (req, res, ex) -> writeError(res, objectMapper, 403, "Недостаточно прав");
    }

    private void writeError(HttpServletResponse res, ObjectMapper mapper,
        int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        mapper.writeValue(res.getWriter(), new ErrorResponse(status, message));
    }

}
