package me.bombom.api.v1.blog.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogImageAssetStatus;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.dto.UploadBlogDraftImageResponse;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.file.dto.StoredFile;
import me.bombom.api.v1.file.service.S3FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@Import({BlogImageService.class, QuerydslConfig.class})
class BlogImageServiceTest {

    @Autowired
    private BlogImageService blogImageService;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogImageAssetRepository blogImageAssetRepository;

    @MockitoBean
    private S3FileService s3FileService;

    @Test
    void 초안_이미지_업로드_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "draft.png", "image/png", "content".getBytes());
        given(s3FileService.uploadToPublicBucketWithMetadata(imageFile, "blog/drafts"))
                .willReturn(new StoredFile("blog/drafts/202603/test.png", "https://cdn/test.png"));

        // when
        UploadBlogDraftImageResponse response = blogImageService.uploadDraftImage(1L, blogPost.getId(), imageFile);

        // then
        BlogImageAsset blogImageAsset = blogImageAssetRepository.findById(response.imageId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(blogImageAsset.getBlogPostId()).isEqualTo(blogPost.getId());
            softly.assertThat(blogImageAsset.getObjectKey()).isEqualTo("blog/drafts/202603/test.png");
            softly.assertThat(blogImageAsset.getImageUrl()).isEqualTo("https://cdn/test.png");
            softly.assertThat(blogImageAsset.getStatus()).isEqualTo(BlogImageAssetStatus.UPLOADED);
            softly.assertThat(blogImageAsset.getDeleteRequestedAt()).isNull();
        });
    }

    @Test
    void 다른_사용자_초안에_이미지_업로드_시_403() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(2L));
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "draft.png", "image/png", "content".getBytes());

        // when & then
        assertThatThrownBy(() -> blogImageService.uploadDraftImage(1L, blogPost.getId(), imageFile))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void DRAFT_아닌_글에_이미지_업로드_시_409() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .status(BlogPostStatus.PUBLISHED)
                .visibility(BlogVisibility.PRIVATE)
                .build());
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "draft.png", "image/png", "content".getBytes());

        // when & then
        assertThatThrownBy(() -> blogImageService.uploadDraftImage(1L, blogPost.getId(), imageFile))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.RESOURCE_CONFLICT);
    }

    private BlogPost createDraftPost(Long memberId) {
        return BlogPost.builder()
                .memberId(memberId)
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build();
    }
}
