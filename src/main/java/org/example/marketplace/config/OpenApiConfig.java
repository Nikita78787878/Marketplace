package org.example.marketplace.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme( // -> "такой способ входа существует"   -> кнопка
    name = "bearerAuth", // внутренний идентификатор, на него ссылаются ниже;
    type = SecuritySchemeType.HTTP,
    scheme = "bearer", // Swagger сам подставит Authorization: Bearer <то, что ввёл>. Приставку писать не надо;
    bearerFormat = "JWT", // чисто подпись для человека, поведения не меняет.
    in = SecuritySchemeIn.HEADER
)

@SecurityScheme(
    name = "keycloak",
    type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "http://localhost:8180/realms/marketplace/protocol/openid-connect/auth",
            tokenUrl = "http://localhost:8180/realms/marketplace/protocol/openid-connect/token",
            scopes = @OAuthScope(name = "openid", description = "OIDC: кто пользователь")
        )
    )
)
@OpenAPIDefinition( // -> "применяй его ко всем ручкам"     -> токен едет
    security = {
        @SecurityRequirement(name = "bearerAuth"),
        @SecurityRequirement(name = "keycloak")  // Можно иначе: вешать @SecurityRequirement(name = "bearerAuth") на каждый контроллер отдельно
    }
)
public class OpenApiConfig {

}
