package me.bombom.api.v1.blog.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.blog.dto.AssignBlogPostThumbnailRequest;
import me.bombom.api.v1.blog.dto.UpdateBlogDraftRequest;
import me.bombom.api.v1.blog.dto.UpdateBlogPostVisibilityRequest;
import me.bombom.api.v1.blog.dto.UploadBlogDraftImageResponse;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.service.BlogDraftService;
import me.bombom.api.v1.blog.service.BlogImageService;
import me.bombom.api.v1.blog.service.BlogPostService;
import me.bombom.api.v1.blog.service.BlogThumbnailService;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
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

    @MockitoBean
    private BlogImageService blogImageService;

    @MockitoBean
    private BlogPostService blogPostService;

    @MockitoBean
    private BlogThumbnailService blogThumbnailService;

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

    @Test
    void 블로그_이미지_업로드_API_성공() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "draft.png", "image/png", "content".getBytes());
        given(blogImageService.uploadPostImage(1L, 123L, imageFile))
                .willReturn(new UploadBlogDraftImageResponse(10L, "https://cdn.bombom.me/blog/10.png"));

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/blog/posts/{postId}/images", 123L)
                        .file(imageFile)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageId").value(10L))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.bombom.me/blog/10.png"));
    }

    @Test
    void 다른_사용자_글_이미지_업로드_시_403을_반환한다() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "draft.png", "image/png", "content".getBytes());
        willThrow(new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE))
                .given(blogImageService).uploadPostImage(1L, 123L, imageFile);

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/blog/posts/{postId}/images", 123L)
                        .file(imageFile)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void 삭제된_글_이미지_업로드_시_409를_반환한다() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "draft.png", "image/png", "content".getBytes());
        willThrow(new CIllegalArgumentException(ErrorDetail.RESOURCE_CONFLICT))
                .given(blogImageService).uploadPostImage(1L, 123L, imageFile);

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/blog/posts/{postId}/images", 123L)
                        .file(imageFile)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void 블로그_글_삭제_API_성공() throws Exception {
        // when // then
        mockMvc.perform(delete("/admin/api/v1/blog/posts/{postId}", 123L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void 블로그_글_공개범위_수정_API_성공() throws Exception {
        // given
        UpdateBlogPostVisibilityRequest request = new UpdateBlogPostVisibilityRequest(BlogVisibility.PUBLIC);

        // when // then
        mockMvc.perform(patch("/admin/api/v1/blog/posts/{postId}/visibility", 123L)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void 잘못된_visibility_값이면_400을_반환한다() throws Exception {
        // when // then
        mockMvc.perform(patch("/admin/api/v1/blog/posts/{postId}/visibility", 123L)
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "visibility": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 블로그_썸네일_등록_API_성공() throws Exception {
        // given
        AssignBlogPostThumbnailRequest request = new AssignBlogPostThumbnailRequest(10L);

        // when // then
        mockMvc.perform(post("/admin/api/v1/blog/posts/{postId}/thumbnail", 123L)
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void imageId가_null이면_400을_반환한다() throws Exception {
        // when // then
        mockMvc.perform(post("/admin/api/v1/blog/posts/{postId}/thumbnail", 123L)
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "imageId": null
                                }
                                """))
                .andExpect(status().isBadRequest());
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
