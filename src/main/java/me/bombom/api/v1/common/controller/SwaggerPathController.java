package me.bombom.api.v1.common.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Hidden
@Controller
public class SwaggerPathController {

    @GetMapping("/admin/v3/api-docs/**")
    public String forwardApiDocs(HttpServletRequest request) {
        return forwardRequest(request, "/admin/v3/api-docs", "/v3/api-docs");
    }

    @GetMapping("/admin/swagger-ui/**")
    public String forwardSwaggerUi(HttpServletRequest request) {
        return forwardRequest(request, "/admin/swagger-ui", "/swagger-ui");
    }

    private String forwardRequest(HttpServletRequest request, String prefix, String forwardPrefix) {
        String path = request.getRequestURI().substring(prefix.length());
        String queryString = request.getQueryString();
        return "forward:" + forwardPrefix + path + (queryString != null ? "?" + queryString : "");
    }
}
