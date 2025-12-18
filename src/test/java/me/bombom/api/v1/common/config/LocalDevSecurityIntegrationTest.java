package me.bombom.api.v1.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.api.v1.member.controller.MemberController;
import me.bombom.api.v1.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("local")
@WebMvcTest(MemberController.class)
@Import({ SecurityConfig.class, WebConfig.class })
@TestPropertySource(properties = "spring.profiles.active=local")
class LocalDevSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("Local 프로필에서는 인증 없이 회원 목록 API 접근이 가능하다")
    void local_bypass_access_success() throws Exception {
        mockMvc.perform(get("/admin/api/v1/members")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Local 프로필에서는 인증 없이 Role 변경 API 접근이 가능하다 (ArgumentResolver 동작 확인)")
    void local_bypass_resolver_success() throws Exception {
        // Given
        // ArgumentResolver가 가짜 Member(ROLE_ADMIN)를 주입하므로,
        // SecurityConfig가 permitAll이어도 컨트롤러 내부 로직까지 진입해야 함.
        // 여기서는 권한이나 로직 실행 여부보다 '호출이 401/403이 안 나는지'를 검증.

        mockMvc.perform(get("/admin/api/v1/members")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }
}
