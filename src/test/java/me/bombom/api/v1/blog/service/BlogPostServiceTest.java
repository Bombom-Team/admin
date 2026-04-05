package me.bombom.api.v1.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogImageAssetStatus;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.config.TimeConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({BlogPostService.class, QuerydslConfig.class, TimeConfig.class})
class BlogPostServiceTest {

    @Autowired
    private BlogPostService blogPostService;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogImageAssetRepository blogImageAssetRepository;

    @Test
    void DRAFT_글_삭제_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DRAFT));

        // when
        blogPostService.deletePost(1L, blogPost.getId());

        // then
        BlogPost deletedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(deletedPost.getStatus()).isEqualTo(BlogPostStatus.DELETED);
    }

    @Test
    void PUBLISHED_글_삭제_성공() {
        // given
        LocalDateTime publishedAt = LocalDateTime.of(2026, 3, 19, 21, 0, 0);
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .status(BlogPostStatus.PUBLISHED)
                .visibility(BlogVisibility.PRIVATE)
                .publishedAt(publishedAt)
                .build());

        // when
        blogPostService.deletePost(1L, blogPost.getId());

        // then
        BlogPost deletedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(deletedPost.getStatus()).isEqualTo(BlogPostStatus.DELETED);
            softly.assertThat(deletedPost.getPublishedAt()).isEqualTo(publishedAt);
        });
    }

    @Test
    void blog_post_status가_DELETED로_바뀐다() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DRAFT));

        // when
        blogPostService.deletePost(1L, blogPost.getId());

        // then
        BlogPost deletedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(deletedPost.getStatus()).isEqualTo(BlogPostStatus.DELETED);
    }

    @Test
    void 해당_post의_모든_image_asset이_DELETE_PENDING으로_바뀐다() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DRAFT));
        BlogImageAsset uploadedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.UPLOADED));
        BlogImageAsset attachedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.ATTACHED));
        BlogImageAsset deletePendingImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.DELETE_PENDING));

        // when
        blogPostService.deletePost(1L, blogPost.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(blogImageAssetRepository.findById(uploadedImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(BlogImageAssetStatus.DELETE_PENDING);
            softly.assertThat(blogImageAssetRepository.findById(attachedImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(BlogImageAssetStatus.DELETE_PENDING);
            softly.assertThat(blogImageAssetRepository.findById(deletePendingImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(BlogImageAssetStatus.DELETE_PENDING);
        });
    }

    @Test
    void delete_requested_at이_세팅된다() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.PUBLISHED));
        BlogImageAsset uploadedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.UPLOADED));

        // when
        blogPostService.deletePost(1L, blogPost.getId());

        // then
        BlogImageAsset deletedImage = blogImageAssetRepository.findById(uploadedImage.getId()).orElseThrow();
        assertThat(deletedImage.getDeleteRequestedAt()).isNotNull();
    }

    @Test
    void 다른_사용자의_글_삭제_시_403() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(2L, BlogPostStatus.DRAFT));

        // when // then
        assertThatThrownBy(() -> blogPostService.deletePost(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 이미_DELETED_상태인_글_삭제_시_409() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DELETED));

        // when // then
        assertThatThrownBy(() -> blogPostService.deletePost(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.RESOURCE_CONFLICT);
    }

    @Test
    void 존재하지_않는_post_삭제_시_404() {
        // when // then
        assertThatThrownBy(() -> blogPostService.deletePost(1L, 999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
    }

    private BlogPost createBlogPost(
            Long memberId,
            BlogPostStatus status
    ) {
        return BlogPost.builder()
                .memberId(memberId)
                .status(status)
                .visibility(BlogVisibility.PRIVATE)
                .build();
    }

    private BlogImageAsset createImage(
            Long postId,
            BlogImageAssetStatus status
    ) {
        return BlogImageAsset.builder()
                .blogPostId(postId)
                .objectKey("blog/posts/" + postId + "/" + status + ".png")
                .imageUrl("https://cdn.bombom.me/" + postId + "/" + status + ".png")
                .status(status)
                .build();
    }
}
