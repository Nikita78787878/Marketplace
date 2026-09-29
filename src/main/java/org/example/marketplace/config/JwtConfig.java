package org.example.marketplace.config;


import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
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

    /**
     * JwtDecoder — компонент, который ПРОВЕРЯЕТ входящий токен.
     * Здесь он только собирается. Работает позже, в рантайме:
     * фильтр зовёт decoder.decode("eyJ...") на каждом запросе.
     *
     * Токен:  eyJhbGci... . eyJzdWIi... . K7dQ2f...
     *         └─ header ─┘  └─ payload ┘  └ signature ┘
     *
     * Что делает decode():
     *
     *   1. ПРОВЕРЯЕТ — здесь нужен ключ.
     *      Берёт строку "header.payload" КАК ЕСТЬ, не раскодируя,
     *      заново считает от неё HMAC нашим ключом и сравнивает
     *      с signature из токена. Расшифровать подпись нельзя,
     *      HMAC односторонний — её можно только пересчитать.
     *      Не сошлось -> JwtException, дальше не идём.
     *
     *   2. ПРОВЕРЯЕТ ВРЕМЯ — exp (не протух) и nbf (не рано).
     *      Это JwtTimestampValidator, стоит внутри по умолчанию.
     *
     *   3. ЧИТАЕТ — ключ не нужен.
     *      Base64-декодирует header и payload обратно в JSON
     *      и достаёт claims: sub, roles, exp.
     *      Эти две части не зашифрованы, их читает кто угодно на jwt.io.
     *
     * Результат: объект Jwt с claims либо JwtException.
     *
     * Порядок важен: сначала проверка, потом чтение. Иначе приложение
     * работало бы с claims из непроверенного токена.
     *
     * Тот же бин SecretKey, что у энкодера — один объект на оба.
     * Разъедутся ключи -> подпись перестанет сходиться без внятной причины.
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey){
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256).build();   // алгоритм фиксируем МЫ, а не header токена.
        // Наивный парсер читает "alg" из самого токена —
        // тогда злоумышленник ставит "alg":"none",
        // выкидывает подпись, и его пропускают.
        // Nimbus так и делает по умолчанию, но пусть
        // решение будет видно в коде, а не подразумевалось.
    }
}
