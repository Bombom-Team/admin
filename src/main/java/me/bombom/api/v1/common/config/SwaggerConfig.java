package me.bombom.api.v1.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class SwaggerConfig {

    // 프로파일 상수
    private static final String PROD_PROFILE = "prod";

    private static final String SECURITY_SCHEME_NAME = "googleOAuth";

    @Value("${swagger.url.prod}")
    private String prodUrl;

    @Value("${swagger.url.local}")
    private String localUrl;

    @Value("${swagger.profile}")
    private String activeProfile;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(createInfo())
                .servers(setApiServer())
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createOAuth2Scheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private List<Server> setApiServer() {
        if (isProfileActive(PROD_PROFILE)) {
            return List.of(createServer(prodUrl, "봄봄 Admin Server"));
        }
        return List.of(createServer(localUrl.concat(serverPort), "봄봄 Local API"));
    }

    private Server createServer(String url, String description) {
        Server server = new Server();
        server.setUrl(url);
        server.setDescription(description);
        return server;
    }

    private boolean isProfileActive(String profile) {
        return activeProfile.equalsIgnoreCase(profile);
    }

    private Info createInfo() {
        String version = getApiVersion();
        String description = getApiDescription();

        return new Info()
                .title("봄봄 Admin API")
                .version(version)
                .description(description);
    }

    private String getApiVersion() {
        if (isProfileActive(PROD_PROFILE)) {
            return "v1.0.0";
        }
        return "v1.0.0-local";
    }

    private String getApiDescription() {
        StringBuilder description = new StringBuilder();
        description.append("봄봄 서비스 관리자 전용 API입니다.");

        if (isProfileActive(PROD_PROFILE)) {
            description.append("\n\n**운영 환경**");
            description.append("\n- 실제 서비스용 Admin API입니다.");
        } else {
            description.append("\n\n**로컬 환경**");
            description.append("\n- 로컬 개발용 API입니다.");
            description.append("\n- 테스트 데이터를 사용합니다.");
        }

        description.append("\n\n**인증 방식**: Google OAuth2 + Session Cookie (JSESSIONID)");
        description.append("\n- 관리자 권한(`ROLE_2`)이 필요합니다.");

        return description.toString();
    }

    private SecurityScheme createOAuth2Scheme() {
        String authUrl = getOAuthAuthorizationUrl();
        String tokenUrl = getOAuthTokenUrl();

        return new SecurityScheme()
                .type(Type.OAUTH2)
                .description(getSecurityDescription())
                .in(In.COOKIE)
                .name("JSESSIONID")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl(authUrl)
                                .tokenUrl(tokenUrl)
                                .scopes(new Scopes()
                                        .addString("openid", "OpenID Connect scope")
                                        .addString("profile", "사용자 프로필 정보")
                                        .addString("email", "사용자 이메일 정보"))));
    }

    private String getOAuthAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth";
    }

    private String getOAuthTokenUrl() {
        return "https://oauth2.googleapis.com/token";
    }

    private String getSecurityDescription() {
        return """
                Google/Apple OAuth2를 통한 인증입니다.

                **사용법**:
                1. '로그인' 버튼을 클릭하여 Google/Apple 로그인을 진행합니다.
                2. 로그인 성공 후 JSESSIONID 쿠키가 설정되며 자동으로 인증됩니다.
                """;
    }
}
