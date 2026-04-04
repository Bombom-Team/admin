package me.bombom.api.v1.blog.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.blog.dto.BlogDraftCategoryResponse;
import me.bombom.api.v1.blog.dto.BlogDraftDetailResponse;
import me.bombom.api.v1.blog.dto.BlogDraftHashtagResponse;
import me.bombom.api.v1.blog.dto.BlogDraftListItemResponse;
import me.bombom.api.v1.blog.dto.BlogDraftReferenceImageResponse;
import me.bombom.api.v1.blog.dto.BlogDraftThumbnailImageResponse;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.blog.dto.UpdateBlogDraftRequest;
import me.bombom.api.v1.blog.dto.UploadBlogDraftImageResponse;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.service.BlogDraftService;
import me.bombom.api.v1.blog.service.BlogImageService;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
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

    @MockitoBean
    private BlogImageService blogImageService;

    @Test
    void 초안_목록_조회_API_성공() throws Exception {
        // given
        given(blogDraftService.getDrafts(1L)).willReturn(List.of(
                new BlogDraftListItemResponse(123L, "제목1", LocalDateTime.of(2026, 3, 19, 21, 0, 0)),
                new BlogDraftListItemResponse(122L, null, LocalDateTime.of(2026, 3, 18, 20, 0, 0))
        ));

        // when // then
        mockMvc.perform(get("/admin/api/v1/blog/drafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId").value(123L))
                .andExpect(jsonPath("$[0].title").value("제목1"))
                .andExpect(jsonPath("$[0].updatedAt").value("2026-03-19T21:00:00"))
                .andExpect(jsonPath("$[1].postId").value(122L));
    }

    @Test
    void 초안_상세_조회_API_성공() throws Exception {
        // given
        given(blogDraftService.getDraft(1L, 123L)).willReturn(new BlogDraftDetailResponse(
                123L,
                "제목",
                "설명",
                "<p>본문</p>",
                BlogPostStatus.DRAFT,
                BlogVisibility.PRIVATE,
                new BlogDraftThumbnailImageResponse(10L, "https://cdn.bombom.me/blog/10.png"),
                new BlogDraftCategoryResponse(1L, "Backend"),
                List.of(new BlogDraftHashtagResponse(1L, "spring")),
                List.of(new BlogDraftReferenceImageResponse(10L, "https://cdn.bombom.me/blog/10.png")),
                LocalDateTime.of(2026, 3, 19, 21, 0, 0)
        ));

        // when // then
        mockMvc.perform(get("/admin/api/v1/blog/drafts/{postId}", 123L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(123L))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.thumbnailImage.imageId").value(10L))
                .andExpect(jsonPath("$.category.id").value(1L))
                .andExpect(jsonPath("$.hashtags[0].name").value("spring"))
                .andExpect(jsonPath("$.referenceImages[0].imageId").value(10L))
                .andExpect(jsonPath("$.updatedAt").value("2026-03-19T21:00:00"));
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

    @Test
    void 초안_이미지_업로드_API_성공() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "draft.png", "image/png", "content".getBytes());
        given(blogImageService.uploadDraftImage(1L, 123L, imageFile))
                .willReturn(new UploadBlogDraftImageResponse(10L, "https://cdn.bombom.me/blog/10.png"));

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/blog/drafts/{postId}/images", 123L)
                        .file(imageFile)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageId").value(10L))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.bombom.me/blog/10.png"));
    }

    @Test
    void 다른_사용자_초안_이미지_업로드_시_403을_반환한다() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "draft.png", "image/png", "content".getBytes());
        willThrow(new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE))
                .given(blogImageService).uploadDraftImage(1L, 123L, imageFile);

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/blog/drafts/{postId}/images", 123L)
                        .file(imageFile)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void 초안이_아닌_글_이미지_업로드_시_409를_반환한다() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "draft.png", "image/png", "content".getBytes());
        willThrow(new CIllegalArgumentException(ErrorDetail.RESOURCE_CONFLICT))
                .given(blogImageService).uploadDraftImage(1L, 123L, imageFile);

        // when // then
        mockMvc.perform(multipart("/admin/api/v1/blog/drafts/{postId}/images", 123L)
                        .file(imageFile)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void 초안_임시저장_API_성공() throws Exception {
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
        mockMvc.perform(put("/admin/api/v1/blog/drafts/{postId}", 123L)
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
