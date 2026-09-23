package org.example.marketplace.security;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 *  Сервис, выпускаюший токен
 *
 *  пример
 *  eyJhbGciOiJIUzI1NiJ9 . eyJzdWIiOiJuaWtpdGEiLCJleHAiOjE3NTUuLi59 . 4pcPyMD09olPSyXnrXCjTw
 * └─── HEADER ───┘   └───────── PAYLOAD ─────────┘   └── SIGNATURE ──┘
 *    alg, typ            claims: sub, exp, iat, roles         подпись первых двух
 *
 * sub	subject	кто это — у нас username
 * iss	issuer	кто выдал токен
 * aud	audience	кому предназначен
 * iat	issued at	когда выдан
 * exp	expiration	до какого момента валиден
 * nbf	not before	раньше этого момента недействителен
 */
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final Duration ttl;

    public JwtService(JwtEncoder encoder, @Value("${jwt.ttl}") Duration ttl) {
        this.encoder = encoder;
        this.ttl = ttl;
    }

    public String generateToken(Authentication authentication){
        Instant now = Instant.now();

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        // payload - что пишется в токен
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("marketplace")
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(authentication.getName())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        //Отдаем на подпись заголовок+значения в наш енкодер который подписывает нашим ключом
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
