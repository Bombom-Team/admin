package me.bombom.api.v1.common.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.config.datasource.DbSwitchInterceptor;
import me.bombom.api.v1.common.resolver.LoginMemberArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final DbSwitchInterceptor dbSwitchInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dbSwitchInterceptor)
                .addPathPatterns("/api/**");
    }
}
