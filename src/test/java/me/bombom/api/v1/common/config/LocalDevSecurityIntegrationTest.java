package me.bombom.api.v1.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.api.v1.member.controller.MemberController;
import me.bombom.api.v1.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("local")
@WebMvcTest(MemberController.class)
@Import({ SecurityConfig.class, WebConfig.class })
@TestPropertySource(properties = {
    "spring.profiles.active=local",
    "server.servlet.session.cookie.max-age=7d",
    "server.servlet.session.cookie.name=TEST_SESSION",
    "server.servlet.session.cookie.domain=localhost"
})
class LocalDevSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void 로컬에서도_비로그인_회원조회는_차단한다() throws Exception {
        // given
        var request = get("/admin/api/v1/members")
                .param("page", "0")
                .param("size", "10");

        // when & then
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    void 로컬에서도_비로그인_권한변경은_차단한다() throws Exception {
        // given
        var request = patch("/admin/api/v1/members/1/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authority\":\"ADMIN\"}");

        // when & then
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "1")
    void 로컬에서도_일반회원의_권한변경은_차단한다() throws Exception {
        // given
        var request = patch("/admin/api/v1/members/1/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authority\":\"ADMIN\"}");

        // when & then
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "2")
    void 로컬에서도_관리자의_권한변경은_허용한다() throws Exception {
        // given
        var request = patch("/admin/api/v1/members/1/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"authority\":\"ADMIN\"}");

        // when & then
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }
}
