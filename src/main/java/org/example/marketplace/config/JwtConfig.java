package org.example.marketplace.config;


import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    private final String secret;

    public JwtConfig(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    /**
     * Секрет из application.yaml -> байты -> контейнер SecretKey.
     * Криптографии здесь нет: SecretKeySpec просто хранит байты
     * с пометкой, для какого алгоритма они предназначены.
     * Один бин на всё приложение: тем же ключом будет
     * проверять подпись JwtDecoder.
     */
    @Bean
    public SecretKey jwtSecretKey(){
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    /**
     * JwtEncoder — компонент, который ПОДПИСЫВАЕТ токены.
     * Здесь он только собирается, ничего не подписывается.
     * ImmutableSecret — простейший JWKSource ("источник ключей"),
     * всегда отдаёт один и тот же секрет.
     * SecurityContext — из com.nimbusds.jose.proc, не спринговый.
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey){
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(jwtSecretKey));
    }
}
