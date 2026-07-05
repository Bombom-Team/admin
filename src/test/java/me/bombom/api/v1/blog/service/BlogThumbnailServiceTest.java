package me.bombom.api.v1.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

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
@Import({BlogThumbnailService.class, QuerydslConfig.class, TimeConfig.class})
class BlogThumbnailServiceTest {

    @Autowired
    private BlogThumbnailService blogThumbnailService;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogImageAssetRepository blogImageAssetRepository;

    @Test
    void DRAFT_글_썸네일_등록_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, null, null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), "https://cdn.bombom.me/blog/1.png", BlogImageAssetStatus.UPLOADED));

        // when
        blogThumbnailService.assignThumbnail(1L, blogPost.getId(), image.getId());

        // then
        BlogPost savedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(savedPost.getThumbnailImageId()).isEqualTo(image.getId());
    }

    @Test
    void PUBLISHED_글_썸네일_등록_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.PUBLISHED, null, null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), "https://cdn.bombom.me/blog/2.png", BlogImageAssetStatus.UPLOADED));

        // when
        blogThumbnailService.assignThumbnail(1L, blogPost.getId(), image.getId());

        // then
        BlogPost savedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(savedPost.getThumbnailImageId()).isEqualTo(image.getId());
    }

    @Test
    void 등록된_썸네일_이미지는_ATTACHED된다() {
        // given
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, null, null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), "https://cdn.bombom.me/blog/3.png", BlogImageAssetStatus.UPLOADED));

        // when
        blogThumbnailService.assignThumbnail(1L, blogPost.getId(), image.getId());

        // then
        BlogImageAsset savedImage = blogImageAssetRepository.findById(image.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedImage.getStatus()).isEqualTo(BlogImageAssetStatus.ATTACHED);
            softly.assertThat(savedImage.getDeleteRequestedAt()).isNull();
        });
    }

    @Test
    void 다른_글_이미지로_썸네일_등록_시_400() {
        // given
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, null, null));
        BlogPost anotherPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, null, null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(anotherPost.getId(), "https://cdn.bombom.me/blog/4.png", BlogImageAssetStatus.UPLOADED));

        // when // then
        assertThatThrownBy(() -> blogThumbnailService.assignThumbnail(1L, blogPost.getId(), image.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    void DELETE_PENDING_이미지는_썸네일_지정_불가() {
        // given
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, null, null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), "https://cdn.bombom.me/blog/5.png", BlogImageAssetStatus.DELETE_PENDING));

        // when // then
        assertThatThrownBy(() -> blogThumbnailService.assignThumbnail(1L, blogPost.getId(), image.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    void 이전_썸네일이_본문에도_없으면_DELETE_PENDING으로_바뀐다() {
        // given
        String oldImageUrl = "https://cdn.bombom.me/blog/old.png";
        String newImageUrl = "https://cdn.bombom.me/blog/new.png";
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, "<p>본문</p>", null));
        BlogImageAsset oldImage = blogImageAssetRepository.save(createImage(blogPost.getId(), oldImageUrl, BlogImageAssetStatus.ATTACHED));
        BlogImageAsset newImage = blogImageAssetRepository.save(createImage(blogPost.getId(), newImageUrl, BlogImageAssetStatus.UPLOADED));
        blogPost.assignThumbnailImage(oldImage.getId());

        // when
        blogThumbnailService.assignThumbnail(1L, blogPost.getId(), newImage.getId());

        // then
        BlogImageAsset savedOldImage = blogImageAssetRepository.findById(oldImage.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedOldImage.getStatus()).isEqualTo(BlogImageAssetStatus.DELETE_PENDING);
            softly.assertThat(savedOldImage.getDeleteRequestedAt()).isNotNull();
        });
    }

    @Test
    void 이전_썸네일도_즉시_DELETE_PENDING으로_바뀐다() {
        // given
        String oldImageUrl = "https://cdn.bombom.me/blog/old-in-content.png";
        String newImageUrl = "https://cdn.bombom.me/blog/new-in-content.png";
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.PUBLISHED, "<p>" + oldImageUrl + "</p>", null));
        BlogImageAsset oldImage = blogImageAssetRepository.save(createImage(blogPost.getId(), oldImageUrl, BlogImageAssetStatus.ATTACHED));
        BlogImageAsset newImage = blogImageAssetRepository.save(createImage(blogPost.getId(), newImageUrl, BlogImageAssetStatus.UPLOADED));
        blogPost.assignThumbnailImage(oldImage.getId());

        // when
        blogThumbnailService.assignThumbnail(1L, blogPost.getId(), newImage.getId());

        // then
        BlogImageAsset savedOldImage = blogImageAssetRepository.findById(oldImage.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedOldImage.getStatus()).isEqualTo(BlogImageAssetStatus.DELETE_PENDING);
            softly.assertThat(savedOldImage.getDeleteRequestedAt()).isNotNull();
        });
    }

    @Test
    void 다른_사용자의_글은_403() {
        // given
        BlogPost blogPost = blogPostRepository.save(createPost(2L, BlogPostStatus.DRAFT, null, null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), "https://cdn.bombom.me/blog/6.png", BlogImageAssetStatus.UPLOADED));

        // when // then
        assertThatThrownBy(() -> blogThumbnailService.assignThumbnail(1L, blogPost.getId(), image.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void DELETED_글은_409() {
        // given
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DELETED, null, null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), "https://cdn.bombom.me/blog/7.png", BlogImageAssetStatus.UPLOADED));

        // when // then
        assertThatThrownBy(() -> blogThumbnailService.assignThumbnail(1L, blogPost.getId(), image.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.RESOURCE_CONFLICT);
    }

    @Test
    void DRAFT_글_썸네일_제거_성공() {
        // given
        String imageUrl = "https://cdn.bombom.me/blog/remove-draft.png";
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, "<p>본문</p>", null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), imageUrl, BlogImageAssetStatus.ATTACHED));
        blogPost.assignThumbnailImage(image.getId());

        // when
        blogThumbnailService.removeThumbnail(1L, blogPost.getId());

        // then
        BlogPost savedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(savedPost.getThumbnailImageId()).isNull();
    }

    @Test
    void PUBLISHED_글_썸네일_제거_성공() {
        // given
        String imageUrl = "https://cdn.bombom.me/blog/remove-published.png";
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.PUBLISHED, "<p>본문</p>", null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), imageUrl, BlogImageAssetStatus.ATTACHED));
        blogPost.assignThumbnailImage(image.getId());

        // when
        blogThumbnailService.removeThumbnail(1L, blogPost.getId());

        // then
        BlogPost savedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(savedPost.getThumbnailImageId()).isNull();
    }

    @Test
    void thumbnail_image_id가_null로_변경된다() {
        // given
        String imageUrl = "https://cdn.bombom.me/blog/remove-null.png";
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, "<p>본문</p>", null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), imageUrl, BlogImageAssetStatus.ATTACHED));
        blogPost.assignThumbnailImage(image.getId());

        // when
        blogThumbnailService.removeThumbnail(1L, blogPost.getId());

        // then
        BlogPost savedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(savedPost.getThumbnailImageId()).isNull();
    }

    @Test
    void 본문에도_없는_기존_썸네일_이미지는_DELETE_PENDING이_된다() {
        // given
        String imageUrl = "https://cdn.bombom.me/blog/remove-delete-pending.png";
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, "<p>본문</p>", null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), imageUrl, BlogImageAssetStatus.ATTACHED));
        blogPost.assignThumbnailImage(image.getId());

        // when
        blogThumbnailService.removeThumbnail(1L, blogPost.getId());

        // then
        BlogImageAsset savedImage = blogImageAssetRepository.findById(image.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedImage.getStatus()).isEqualTo(BlogImageAssetStatus.DELETE_PENDING);
            softly.assertThat(savedImage.getDeleteRequestedAt()).isNotNull();
        });
    }

    @Test
    void 기존_썸네일_이미지는_본문과_무관하게_DELETE_PENDING이_된다() {
        // given
        String imageUrl = "https://cdn.bombom.me/blog/remove-attached.png";
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.PUBLISHED, "<p>" + imageUrl + "</p>", null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), imageUrl, BlogImageAssetStatus.ATTACHED));
        blogPost.assignThumbnailImage(image.getId());

        // when
        blogThumbnailService.removeThumbnail(1L, blogPost.getId());

        // then
        BlogImageAsset savedImage = blogImageAssetRepository.findById(image.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedImage.getStatus()).isEqualTo(BlogImageAssetStatus.DELETE_PENDING);
            softly.assertThat(savedImage.getDeleteRequestedAt()).isNotNull();
        });
    }

    @Test
    void 원래_썸네일이_없어도_204_의미로_성공한다() {
        // given
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DRAFT, "<p>본문</p>", null));

        // when
        blogThumbnailService.removeThumbnail(1L, blogPost.getId());

        // then
        BlogPost savedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(savedPost.getThumbnailImageId()).isNull();
    }

    @Test
    void 다른_사용자의_글_썸네일_제거_시_403() {
        // given
        String imageUrl = "https://cdn.bombom.me/blog/remove-forbidden.png";
        BlogPost blogPost = blogPostRepository.save(createPost(2L, BlogPostStatus.DRAFT, "<p>본문</p>", null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), imageUrl, BlogImageAssetStatus.ATTACHED));
        blogPost.assignThumbnailImage(image.getId());

        // when // then
        assertThatThrownBy(() -> blogThumbnailService.removeThumbnail(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void DELETED_글_썸네일_제거_시_409() {
        // given
        String imageUrl = "https://cdn.bombom.me/blog/remove-conflict.png";
        BlogPost blogPost = blogPostRepository.save(createPost(1L, BlogPostStatus.DELETED, "<p>본문</p>", null));
        BlogImageAsset image = blogImageAssetRepository.save(createImage(blogPost.getId(), imageUrl, BlogImageAssetStatus.ATTACHED));
        blogPost.assignThumbnailImage(image.getId());

        // when // then
        assertThatThrownBy(() -> blogThumbnailService.removeThumbnail(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.RESOURCE_CONFLICT);
    }

    @Test
    void 존재하지_않는_post는_404() {
        // when // then
        assertThatThrownBy(() -> blogThumbnailService.removeThumbnail(1L, 999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
    }

    private BlogPost createPost(
            Long memberId,
            BlogPostStatus status,
            String content,
            Long thumbnailImageId
    ) {
        return BlogPost.builder()
                .memberId(memberId)
                .content(content)
                .thumbnailImageId(thumbnailImageId)
                .status(status)
                .visibility(BlogVisibility.PRIVATE)
                .build();
    }

    private BlogImageAsset createImage(
            Long postId,
            String imageUrl,
            BlogImageAssetStatus status
    ) {
        return BlogImageAsset.builder()
                .blogPostId(postId)
                .objectKey("blog/posts/" + postId + "/" + status + ".png")
                .imageUrl(imageUrl)
                .status(status)
                .build();
    }
}
