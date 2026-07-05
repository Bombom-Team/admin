package me.bombom.api.v1.blog.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.blog.dto.BlogDraftListItemResponse;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.blog.service.BlogDraftService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(controllers = BlogDraftController.class)
@Import(BlogDraftControllerTest.LoginMemberResolverTestConfig.class)
class BlogDraftControllerTest extends ControllerTestSupport {

    @MockitoBean
    private BlogDraftService blogDraftService;

    @Test
    void 초안_목록_조회_API_성공() throws Exception {
        // given
        given(blogDraftService.getDrafts(1L)).willReturn(List.of(
                new BlogDraftListItemResponse(123L, "제목1", LocalDateTime.of(2026, 3, 19, 21, 0, 0)),
                new BlogDraftListItemResponse(122L, null, LocalDateTime.of(2026, 3, 18, 20, 0, 0))
        ));

        // when // then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/api/v1/blog/drafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId").value(123L))
                .andExpect(jsonPath("$[0].title").value("제목1"))
                .andExpect(jsonPath("$[0].updatedAt").value("2026-03-19T21:00:00"))
                .andExpect(jsonPath("$[1].postId").value(122L));
    }

    @Test
    void 초안_발행_API_성공() throws Exception {
        // when // then
        mockMvc.perform(post("/admin/api/v1/blog/drafts/{postId}/publish", 123L)
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void 초안_생성_API_성공() throws Exception {
        // given
        given(blogDraftService.createDraft(1L)).willReturn(new CreateBlogDraftResponse(123L));

        // when // then
        mockMvc.perform(post("/admin/api/v1/blog/drafts").with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(123L));
    }

    @TestConfiguration
    static class LoginMemberResolverTestConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(LoginMember.class)
                            && parameter.getParameterType().equals(Member.class);
                }

                @Override
                public Object resolveArgument(
                        MethodParameter parameter,
                        ModelAndViewContainer mavContainer,
                        NativeWebRequest webRequest,
                        WebDataBinderFactory binderFactory
                ) {
                    return Member.builder()
                            .id(1L)
                            .provider("local")
                            .providerId("local")
                            .email("local@bombom.me")
                            .nickname("local-admin")
                            .gender(Gender.MALE)
                            .roleId(2L)
                            .build();
                }
            });
        }
    }
}
