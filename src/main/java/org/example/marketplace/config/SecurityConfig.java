package org.example.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll() // доступ для всех
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").authenticated() // залогиненнный
                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN") // только админу
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                .anyRequest().denyAll() // все остальное запрещено
                ).httpBasic(Customizer.withDefaults()) //  Включает HTTP Basic Authentication — самый простой стандартный способ представиться.
                // Логин и пароль склеиваются в логин:пароль, кодируются в Base64 и едут в заголовке каждого запроса

        .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * Кодировщик кодирует что в БД не лежал в открытую пароль
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     *
     * Делаем для работы по JWT токену
     *
     * AuthenticationManager - интерфейс, одна точка входа для проверки:
     *   Authentication authenticate(Authentication) - на входе непроверенный
     *   конверт (логин+пароль), на выходе проверенный (кто + роли) или исключение.
     *   
     *    ProviderManager - реализация. Сам не проверяет, держит список провайдеров
     *   и отдаёт конверт тому, кто умеет с таким типом работать (supports()).
     *
     * DaoAuthenticationProvider  - настоящая проверка: просит юзера у UserDetailsService,
     * зовёт PasswordEncoder, проверяет флаги, принимает решение
     *
     * UserDetailsService - дай юзера по логину
     *
     * PasswordEncoder - совпадает ли пароль с этим хешем
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder){

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);

    }

}
