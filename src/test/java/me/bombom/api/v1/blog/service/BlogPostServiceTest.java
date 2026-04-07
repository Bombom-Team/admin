package me.bombom.api.v1.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogHashtag;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogImageAssetStatus;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogPostTag;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.dto.BlogPostDetailResponse;
import me.bombom.api.v1.blog.dto.BlogPostListItemResponse;
import me.bombom.api.v1.blog.repository.BlogCategoryRepository;
import me.bombom.api.v1.blog.repository.BlogHashtagRepository;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.blog.repository.BlogPostTagRepository;
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
    private BlogCategoryRepository blogCategoryRepository;

    @Autowired
    private BlogHashtagRepository blogHashtagRepository;

    @Autowired
    private BlogPostTagRepository blogPostTagRepository;

    @Autowired
    private BlogImageAssetRepository blogImageAssetRepository;

    @Test
    void 블로그_글_목록은_작성자와_무관하게_조회된다() {
        // given
        BlogPost firstPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("첫 번째 글")
                .description("첫 번째 부제")
                .status(BlogPostStatus.PUBLISHED)
                .visibility(BlogVisibility.PUBLIC)
                .publishedAt(LocalDateTime.of(2026, 3, 19, 21, 0, 0))
                .build());
        BlogPost secondPost = blogPostRepository.save(BlogPost.builder()
                .memberId(2L)
                .title("두 번째 글")
                .description("두 번째 부제")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when
        List<BlogPostListItemResponse> responses = blogPostService.getPosts(null);

        // then
        assertThat(responses).extracting(BlogPostListItemResponse::postId)
                .contains(firstPost.getId(), secondPost.getId());
        assertThat(responses).extracting(BlogPostListItemResponse::memberId)
                .contains(1L, 2L);
        assertThat(responses).extracting(BlogPostListItemResponse::description)
                .contains("첫 번째 부제", "두 번째 부제");
    }

    @Test
    void 블로그_글_목록은_visibility로_필터링된다() {
        // given
        blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("공개 글")
                .description("공개 부제")
                .status(BlogPostStatus.PUBLISHED)
                .visibility(BlogVisibility.PUBLIC)
                .publishedAt(LocalDateTime.of(2026, 3, 19, 21, 0, 0))
                .build());
        blogPostRepository.save(BlogPost.builder()
                .memberId(2L)
                .title("비공개 글")
                .description("비공개 부제")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when
        List<BlogPostListItemResponse> responses = blogPostService.getPosts(BlogVisibility.PUBLIC);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).visibility()).isEqualTo(BlogVisibility.PUBLIC);
        assertThat(responses.get(0).description()).isEqualTo("공개 부제");
    }

    @Test
    void 블로그_글_상세_조회_성공() {
        // given
        BlogCategory blogCategory = blogCategoryRepository.save(BlogCategory.builder()
                .name("Backend")
                .build());
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(2L)
                .title("제목")
                .description("설명")
                .content("<p>본문</p>")
                .status(BlogPostStatus.PUBLISHED)
                .visibility(BlogVisibility.PUBLIC)
                .categoryId(blogCategory.getId())
                .publishedAt(LocalDateTime.of(2026, 3, 19, 21, 0, 0))
                .build());
        BlogHashtag blogHashtag = blogHashtagRepository.save(BlogHashtag.builder()
                .name("spring")
                .build());
        blogPostTagRepository.save(BlogPostTag.builder()
                .blogPostId(blogPost.getId())
                .blogHashtagId(blogHashtag.getId())
                .build());
        BlogImageAsset thumbnailImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.ATTACHED));

        BlogPost savedBlogPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        savedBlogPost.assignThumbnailImage(thumbnailImage.getId());

        // when
        BlogPostDetailResponse response = blogPostService.getPost(blogPost.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.title()).isEqualTo("제목");
            softly.assertThat(response.description()).isEqualTo("설명");
            softly.assertThat(response.content()).isEqualTo("<p>본문</p>");
            softly.assertThat(response.thumbnailImageUrl()).isEqualTo(thumbnailImage.getImageUrl());
            softly.assertThat(response.categoryName()).isEqualTo("Backend");
            softly.assertThat(response.publishedAt()).isEqualTo(LocalDateTime.of(2026, 3, 19, 21, 0, 0));
            softly.assertThat(response.hashtags()).containsExactly("spring");
        });
    }

    @Test
    void 존재하지_않는_블로그_글_상세_조회_시_404() {
        // when // then
        assertThatThrownBy(() -> blogPostService.getPost(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
    }

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

    @Test
    void DRAFT_글_visibility_변경_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DRAFT));

        // when
        blogPostService.updatePostVisibility(1L, blogPost.getId(), BlogVisibility.PUBLIC);

        // then
        BlogPost updatedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(updatedPost.getVisibility()).isEqualTo(BlogVisibility.PUBLIC);
    }

    @Test
    void PUBLISHED_글_visibility_변경_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.PUBLISHED));

        // when
        blogPostService.updatePostVisibility(1L, blogPost.getId(), BlogVisibility.PUBLIC);

        // then
        BlogPost updatedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(updatedPost.getVisibility()).isEqualTo(BlogVisibility.PUBLIC);
    }

    @Test
    void PRIVATE에서_PUBLIC으로_변경_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DRAFT));

        // when
        blogPostService.updatePostVisibility(1L, blogPost.getId(), BlogVisibility.PUBLIC);

        // then
        BlogPost updatedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(updatedPost.getVisibility()).isEqualTo(BlogVisibility.PUBLIC);
    }

    @Test
    void PUBLIC에서_PRIVATE로_변경_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .status(BlogPostStatus.PUBLISHED)
                .visibility(BlogVisibility.PUBLIC)
                .build());

        // when
        blogPostService.updatePostVisibility(1L, blogPost.getId(), BlogVisibility.PRIVATE);

        // then
        BlogPost updatedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(updatedPost.getVisibility()).isEqualTo(BlogVisibility.PRIVATE);
    }

    @Test
    void 다른_사용자_글_visibility_변경_시_403() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(2L, BlogPostStatus.DRAFT));

        // when // then
        assertThatThrownBy(() -> blogPostService.updatePostVisibility(1L, blogPost.getId(), BlogVisibility.PUBLIC))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void DELETED_글_visibility_변경_시_409() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DELETED));

        // when // then
        assertThatThrownBy(() -> blogPostService.updatePostVisibility(1L, blogPost.getId(), BlogVisibility.PUBLIC))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.RESOURCE_CONFLICT);
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
