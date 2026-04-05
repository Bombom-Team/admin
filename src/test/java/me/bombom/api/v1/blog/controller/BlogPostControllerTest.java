package me.bombom.api.v1.blog.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.blog.dto.UpdateBlogDraftRequest;
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

@WebMvcTest(controllers = BlogPostController.class)
@Import(BlogPostControllerTest.LoginMemberResolverTestConfig.class)
class BlogPostControllerTest extends ControllerTestSupport {

    @MockitoBean
    private BlogDraftService blogDraftService;

    @Test
    void 블로그_글_수정_API_성공() throws Exception {
        // given
        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                "제목",
                "본문",
                "설명",
                3L,
                List.of("봄봄"),
                List.of(10L, 11L)
        );

        // when // then
        mockMvc.perform(put("/admin/api/v1/blog/posts/{postId}", 123L)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
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
