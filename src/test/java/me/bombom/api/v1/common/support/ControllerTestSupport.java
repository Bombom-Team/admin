package me.bombom.api.v1.common.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bombom.api.v1.common.config.SecurityConfig;
import me.bombom.api.v1.common.exception.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ActiveProfiles("local")
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
@WithMockUser(roles = "2")
@TestPropertySource(properties = {
    "server.servlet.session.cookie.max-age=7d",
    "server.servlet.session.cookie.name=TEST_SESSION",
    "server.servlet.session.cookie.domain=localhost"
})
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JpaMetamodelMappingContext jpaMetamodelMappingContext;
}
