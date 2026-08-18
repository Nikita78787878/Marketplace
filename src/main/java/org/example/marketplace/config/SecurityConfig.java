package org.example.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //справочник пользователей
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder){

        //  UserDetails внутряняя штука security имя, пароль, роль, залочен или нет и так далее
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("123456"))
                .roles("ADMIN")
                .build();

        UserDetails nikita = User.withUsername("nikita")
                .password(encoder.encode("123456")) // security неползвоялет хранить пароли в открытом досступе
                // хеширует пароль от юзака и то что лежит в бд и сравнивает, обратно из хеша восстановить не получиться
                .roles("USER")

                .build();

        // Сохрнаяет в оперативной памяти
        return new InMemoryUserDetailsManager(admin, nikita);

    }
}
