package news.bombomadmin.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Value("${swagger.admin.username}")
//    private String adminUsername;
//
//    @Value("${swagger.admin.password}")
//    private String adminPassword;
//
//    @Value("${server.servlet.session.cookie.max-age}")
//    private Duration cookieMaxAge;
//
//    @Bean
//    public SecurityFilterChain apiSecurityFilterChain(
//            HttpSecurity http,
//            CustomOAuth2UserService customOAuth2UserService,
//            AppleOAuth2Service appleOAuth2Service,
//            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
//            OAuth2LoginFailureHandler oAuth2LoginFailureHandler,
//            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegatingAccessTokenClient,
//            ClientRegistrationRepository clientRegistrationRepository
//    ) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
//                .formLogin(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
//                .oauth2Login(oauth2 -> oauth2
//                        .authorizationEndpoint(authorization ->
//                                authorization.authorizationRequestResolver(new AppleAuthorizationRequestResolver(clientRegistrationRepository))
//                        )
//                        .tokenEndpoint(token -> token.accessTokenResponseClient(delegatingAccessTokenClient))
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(customOAuth2UserService)
//                                .oidcUserService(appleOAuth2Service))
//                        .successHandler(oAuth2LoginSuccessHandler)
//                        .failureHandler(oAuth2LoginFailureHandler));
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.addAllowedOriginPattern("*"); // 모든 origin 허용, 필요에 따라 특정 origin으로 변경 가능
//        configuration.addAllowedMethod("*");
//        configuration.addAllowedHeader("*"); // 모든 헤더 허용
//        configuration.setAllowCredentials(true);
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = User.builder()
//                .username(adminUsername)
//                .password(passwordEncoder().encode(adminPassword))
//                .roles("DEVELOPER")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public ECPrivateKey applePrivateKey(
//            @Value("${oauth2.apple.private-key}") String privateKeyPem
//    ) {
//        return new ApplePrivateKeyLoader().loadFromPem(privateKeyPem);
//    }
//
//    @Bean
//    public AppleClientSecretSupplier appleClientSecretSupplier(
//            @Value("${oauth2.apple.team-id}") String teamId,
//            @Value("${oauth2.apple.key-id}") String keyId,
//            @Value("${oauth2.apple.client-id}") String clientId,
//            ECPrivateKey applePrivateKey
//    ) {
//        return new AppleClientSecretSupplier(teamId, keyId, clientId, applePrivateKey);
//    }
//
//    @Bean
//    public AppleOAuth2AccessTokenResponseClient appleOAuth2AccessTokenResponseClient(
//            Supplier<String> appleClientSecretSupplier,
//            RestClient.Builder restClientBuilder
//    ) {
//        return new AppleOAuth2AccessTokenResponseClient(appleClientSecretSupplier, restClientBuilder.build());
//    }
//
//    @Bean
//    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegatingAccessTokenClient(
//            AppleOAuth2AccessTokenResponseClient appleClient
//    ) {
//        var defaultClient = new RestClientAuthorizationCodeTokenResponseClient();
//        return request -> {
//            String registrationId = request.getClientRegistration().getRegistrationId();
//            if ("apple".equals(registrationId)) {
//                return appleClient.getTokenResponse(request);
//            }
//            return defaultClient.getTokenResponse(request);
//        };
//    }
//
//    @Bean
//    public JwtDecoder jwtDecoder(
//            @Value("${spring.security.oauth2.client.provider.apple.jwk-set-uri}") String jwkSetUri,
//            @Value("${spring.security.oauth2.client.registration.apple.client-id}") String audience) {
//
//        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation("https://appleid.apple.com");
//
//        // Audience(aud) 클레임 검증을 위한 Validator 추가
//        OAuth2TokenValidator<Jwt> audienceValidator = token -> {
//            return token.getAudience().contains(audience)
//                    ? org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
//                    : org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(new org.springframework.security.oauth2.core.OAuth2Error("invalid_token", "The required audience is missing", null));
//        };
//
//        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer("https://appleid.apple.com");
//        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
//
//        jwtDecoder.setJwtValidator(withAudience);
//        return jwtDecoder;
//    }
//
//    @Bean
//    public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory(JwtDecoder jwtDecoder) {
//        // JwtDecoderFactory를 Bean으로 직접 등록합니다.
//        // Spring Security는 이 Bean을 자동으로 사용하여 ID 토큰을 검증합니다.
//        return clientRegistration -> {
//            if (clientRegistration.getRegistrationId().equals("apple")) {
//                // "apple" 로그인일 경우, 우리가 만든 audience 검증 기능이 포함된 JwtDecoder를 사용합니다.
//                return jwtDecoder;
//            }
//            // 다른 OIDC 제공자(예: Google)는 Spring Security의 기본 디코더를 사용합니다.
//            return JwtDecoders.fromOidcIssuerLocation(clientRegistration.getProviderDetails().getIssuerUri());
//        };
//    }
//
//    @Bean
//    public CookieSerializer cookieSerializer() {
//        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
//        serializer.setCookieName("JSESSIONID");
//        serializer.setUseHttpOnlyCookie(true);
//        serializer.setUseSecureCookie(true);
//        serializer.setSameSite("None");
//        serializer.setCookiePath("/");
//        serializer.setCookieMaxAge((int) cookieMaxAge.getSeconds());
//        return serializer;
//    }
}

