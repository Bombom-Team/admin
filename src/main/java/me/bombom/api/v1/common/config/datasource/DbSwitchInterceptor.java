package me.bombom.api.v1.common.config.datasource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class DbSwitchInterceptor implements HandlerInterceptor {

    private static final String HEADER_TARGET_DB = "X-Target-DB";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String targetDb = request.getHeader(HEADER_TARGET_DB);
        if (DataSourceContextHolder.DEV.equalsIgnoreCase(targetDb)) {
            DataSourceContextHolder.setContext(DataSourceContextHolder.DEV);
            log.info("Switching to DEV DataSource for request: {}", request.getRequestURI());
        } else {
            DataSourceContextHolder.setContext(DataSourceContextHolder.PROD);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        DataSourceContextHolder.clear();
    }
}
