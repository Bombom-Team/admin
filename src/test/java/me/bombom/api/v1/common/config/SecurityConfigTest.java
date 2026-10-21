package me.bombom.api.v1.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class SecurityConfigTest {

    @ParameterizedTest
    @ValueSource(strings = {"local", "prod,admin"})
    void 비로그인은_모든_API와_운영경로에서_차단한다(String profile) throws Exception {
        // given
        try (var context = createContext(profile)) {
            MockMvc mockMvc = createMockMvc(context);
            List<ApiRoute> routes = getRoutes(context);

            // when & then
            assertAccess(mockMvc, routes, request -> request, 403);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"local", "prod,admin"})
    void 일반회원은_모든_API와_운영경로에서_차단한다(String profile) throws Exception {
        // given
        try (var context = createContext(profile)) {
            MockMvc mockMvc = createMockMvc(context);
            List<ApiRoute> routes = getRoutes(context);

            // when & then
            assertAccess(mockMvc, routes, user("member").roles("1"), 403);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"local", "prod,admin"})
    void 관리자는_모든_API와_운영경로의_권한검사를_통과한다(String profile) throws Exception {
        // given
        try (var context = createContext(profile)) {
            MockMvc mockMvc = createMockMvc(context);
            List<ApiRoute> routes = getRoutes(context);

            // when & then
            assertAccess(mockMvc, routes, user("admin").roles("2"), 200);
        }
    }

    private AnnotationConfigWebApplicationContext createContext(String profile) {
        var context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertyValues.of(
                "spring.profiles.active=" + profile,
                "server.servlet.session.cookie.name=TEST_SESSION",
                "server.servlet.session.cookie.domain=localhost",
                "server.servlet.session.cookie.max-age=7d"
        ).applyTo(context);
        context.register(SecurityTestConfiguration.class);
        context.refresh();
        return context;
    }

    private MockMvc createMockMvc(AnnotationConfigWebApplicationContext context) {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private List<ApiRoute> getRoutes(AnnotationConfigWebApplicationContext context) throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        var inspector = new MappingInspector();
        inspector.setApplicationContext(context);
        inspector.afterPropertiesSet();
        List<ApiRoute> routes = new ArrayList<>();
        for (var definition : scanner.findCandidateComponents("me.bombom.api.v1")) {
            Class<?> controller = Class.forName(definition.getBeanClassName());
            for (Method method : controller.getDeclaredMethods()) {
                RequestMappingInfo mapping = inspector.mapping(method, controller);
                if (mapping == null) {
                    continue;
                }
                for (String path : mapping.getPatternValues()) {
                    for (RequestMethod verb : mapping.getMethodsCondition().getMethods()) {
                        routes.add(new ApiRoute(HttpMethod.valueOf(verb.name()), path));
                    }
                }
            }
        }
        assertThat(routes).isNotEmpty();
        List<String> infrastructurePaths = List.of(
                "/swagger-ui/index.html", "/v3/api-docs", "/actuator/health",
                "/admin/v3/api-docs", "/admin/swagger-ui/index.html", "/admin/swagger-ui.html",
                "/admin/api-docs/openapi.json", "/actuator/prometheus", "/future/api", "/logout"
        );
        infrastructurePaths.forEach(path -> routes.add(new ApiRoute(HttpMethod.GET, path)));
        return routes;
    }

    private void assertAccess(
            MockMvc mockMvc,
            List<ApiRoute> routes,
            RequestPostProcessor identity,
            int expectedStatus) throws Exception {
        for (ApiRoute route : routes) {
            String path = route.path().replaceAll("\\{[^}]+}", "1");
            int status = mockMvc.perform(request(route.method(), path).with(identity))
                    .andReturn()
                    .getResponse()
                    .getStatus();
            assertThat(status).as("%s %s", route.method(), route.path()).isEqualTo(expectedStatus);
        }
    }

    private record ApiRoute(
            HttpMethod method,
            String path
    ) {
    }

    private static class MappingInspector extends RequestMappingHandlerMapping {

        RequestMappingInfo mapping(Method method, Class<?> controller) {
            return getMappingForMethod(method, controller);
        }
    }

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, AuthorizationProbe.class})
    static class SecurityTestConfiguration {

        @Bean
        static ConversionService conversionService() {
            return new ApplicationConversionService();
        }
    }

    // Business handlers are replaced so authorization checks cannot read or mutate application data.
    @RestController
    static class AuthorizationProbe {

        @RequestMapping("/**")
        ProbeResponse probe() {
            return new ProbeResponse("authorized");
        }
    }

    record ProbeResponse(
            String status
    ) {
    }
}
