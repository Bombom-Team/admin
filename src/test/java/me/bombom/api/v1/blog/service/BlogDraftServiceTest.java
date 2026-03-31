package me.bombom.api.v1.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogHashtag;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogImageAssetStatus;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogPostTag;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.blog.dto.UpdateBlogDraftRequest;
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
@Import({BlogDraftService.class, QuerydslConfig.class, TimeConfig.class})
class BlogDraftServiceTest {

    @Autowired
    private BlogDraftService blogDraftService;

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
    void 초안_생성_성공() {
        // when
        CreateBlogDraftResponse response = blogDraftService.createDraft(1L);

        // then
        BlogPost blogPost = blogPostRepository.findById(response.postId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(blogPost.getMemberId()).isEqualTo(1L);
            softly.assertThat(blogPost.getTitle()).isNull();
            softly.assertThat(blogPost.getContent()).isNull();
            softly.assertThat(blogPost.getDescription()).isNull();
            softly.assertThat(blogPost.getThumbnailImageId()).isNull();
            softly.assertThat(blogPost.getCategoryId()).isNull();
            softly.assertThat(blogPost.getStatus()).isEqualTo(BlogPostStatus.DRAFT);
            softly.assertThat(blogPost.getVisibility()).isEqualTo(BlogVisibility.PRIVATE);
            softly.assertThat(blogPost.getPublishedAt()).isNull();
        });
    }

    @Test
    void 초안_저장_시_해시태그_신규_생성_기존_태그_교체_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));
        BlogHashtag oldHashTag = blogHashtagRepository.save(BlogHashtag.builder()
                .name("이전태그")
                .build());
        blogPostTagRepository.save(BlogPostTag.builder()
                .blogPostId(blogPost.getId())
                .blogHashtagId(oldHashTag.getId())
                .build());

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                "제목",
                "본문",
                "설명",
                null,
                List.of(" 뉴스레터 ", "추천", "", "추천", "봄봄"),
                null
        );

        // when
        blogDraftService.updateDraft(1L, blogPost.getId(), request);

        // then
        List<String> savedHashTagNames = blogPostTagRepository.findAllByBlogPostId(blogPost.getId()).stream()
                .map(BlogPostTag::getBlogHashtagId)
                .map(hashTagId -> blogHashtagRepository.findById(hashTagId).orElseThrow().getName())
                .toList();

        assertThat(savedHashTagNames).containsExactly("뉴스레터", "추천", "봄봄");
        assertThat(blogHashtagRepository.findByName("이전태그")).isPresent();
    }

    @Test
    void 초안_저장_시_referencedImageIds에_포함된_이미지가_ATTACHED로_전환() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));
        BlogImageAsset uploadedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.UPLOADED));

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                null,
                null,
                null,
                null,
                null,
                List.of(uploadedImage.getId())
        );

        // when
        blogDraftService.updateDraft(1L, blogPost.getId(), request);

        // then
        BlogImageAsset savedImage = blogImageAssetRepository.findById(uploadedImage.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedImage.getStatus()).isEqualTo(BlogImageAssetStatus.ATTACHED);
            softly.assertThat(savedImage.getDeleteRequestedAt()).isNull();
        });
    }

    @Test
    void 기존_ATTACHED_이미지가_요청에서_빠지면_DELETE_PENDING으로_전환() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));
        BlogImageAsset attachedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.ATTACHED));

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                null,
                null,
                null,
                null,
                null,
                List.of()
        );

        // when
        blogDraftService.updateDraft(1L, blogPost.getId(), request);

        // then
        BlogImageAsset savedImage = blogImageAssetRepository.findById(attachedImage.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedImage.getStatus()).isEqualTo(BlogImageAssetStatus.DELETE_PENDING);
            softly.assertThat(savedImage.getDeleteRequestedAt()).isNotNull();
        });
    }

    @Test
    void UPLOADED_이미지가_요청에서_빠지면_그대로_UPLOADED_유지() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));
        BlogImageAsset uploadedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.UPLOADED));

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                null,
                null,
                null,
                null,
                null,
                List.of()
        );

        // when
        blogDraftService.updateDraft(1L, blogPost.getId(), request);

        // then
        BlogImageAsset savedImage = blogImageAssetRepository.findById(uploadedImage.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedImage.getStatus()).isEqualTo(BlogImageAssetStatus.UPLOADED);
            softly.assertThat(savedImage.getDeleteRequestedAt()).isNull();
        });
    }

    @Test
    void 다른_글의_이미지_ID를_referencedImageIds로_보내면_400() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));
        BlogPost anotherBlogPost = blogPostRepository.save(createDraftPost(1L));
        BlogImageAsset anotherPostImage = blogImageAssetRepository.save(createImage(anotherBlogPost.getId(), BlogImageAssetStatus.UPLOADED));

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                null,
                null,
                null,
                null,
                null,
                List.of(anotherPostImage.getId())
        );

        // when // then
        assertThatThrownBy(() -> blogDraftService.updateDraft(1L, blogPost.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    void 존재하지_않는_categoryId_저장_시_404() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));
        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                null,
                null,
                null,
                999L,
                null,
                null
        );

        // when // then
        assertThatThrownBy(() -> blogDraftService.updateDraft(1L, blogPost.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 초안_저장_시_기본필드와_카테고리_저장_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));
        BlogCategory blogCategory = blogCategoryRepository.save(BlogCategory.builder()
                .name("카테고리")
                .build());

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                "수정된 제목",
                "수정된 본문",
                "수정된 설명",
                blogCategory.getId(),
                null,
                List.of()
        );

        // when
        blogDraftService.updateDraft(1L, blogPost.getId(), request);

        // then
        BlogPost savedBlogPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedBlogPost.getTitle()).isEqualTo("수정된 제목");
            softly.assertThat(savedBlogPost.getContent()).isEqualTo("수정된 본문");
            softly.assertThat(savedBlogPost.getDescription()).isEqualTo("수정된 설명");
            softly.assertThat(savedBlogPost.getThumbnailImageId()).isNull();
            softly.assertThat(savedBlogPost.getCategoryId()).isEqualTo(blogCategory.getId());
            softly.assertThat(savedBlogPost.getStatus()).isEqualTo(BlogPostStatus.DRAFT);
            softly.assertThat(savedBlogPost.getVisibility()).isEqualTo(BlogVisibility.PRIVATE);
        });
    }

    private BlogPost createDraftPost(Long memberId) {
        return BlogPost.builder()
                .memberId(memberId)
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build();
    }

    private BlogImageAsset createImage(Long postId, BlogImageAssetStatus status) {
        return BlogImageAsset.builder()
                .blogPostId(postId)
                .objectKey("blog/drafts/" + postId + "/" + status + ".png")
                .imageUrl("https://cdn.bombom.me/" + postId + "/" + status + ".png")
                .status(status)
                .build();
    }
}
