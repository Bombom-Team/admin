package news.bombomadmin.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import news.bombomadmin.member.dto.GetMemberResponse;
import news.bombomadmin.member.dto.MembersOptionsRequest;
import news.bombomadmin.member.dto.UpdateRoleRequest;
import news.bombomadmin.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void 회원_목록_조회_name_role_필터_페이징_적용() throws Exception {
        // given
        String name = "kim";
        String email = "kim@email.com";
        String role = "ADMIN";
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<GetMemberResponse> page = new PageImpl<>(
                List.of(new GetMemberResponse(1L, name, email, role)),
                pageRequest,
                1
        );

        given(memberService.getMembers(
                argThat(req -> req.name().equals(name) && req.role().equals(role)),
                any(PageRequest.class)
        )).willReturn(page);

        // when, then
        mockMvc.perform(get("/admin/members")
                        .param("name", name)
                        .param("role", role)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nickname").value(name))
                .andExpect(jsonPath("$.content[0].role").value(role));

        verify(memberService).getMembers(any(MembersOptionsRequest.class), any(PageRequest.class));
    }

    @Test
    void 회원_역할_변경() throws Exception {
        // given
        Long memberId = 10L;
        String authority = "ADMIN";
        String body = """
                {"authority":"%s"}
                """.formatted(authority);

        // when, then
        mockMvc.perform(patch("/admin/members/{id}/role", memberId)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNoContent());

        verify(memberService).updateRole(eq(memberId), any(UpdateRoleRequest.class));
    }
}

