package me.bombom.api.v1.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import jakarta.persistence.EntityManager;
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
import me.bombom.api.v1.blog.dto.BlogDraftDetailResponse;
import me.bombom.api.v1.blog.dto.BlogDraftListItemResponse;
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

    @Autowired
    private EntityManager entityManager;

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
        blogDraftService.updatePost(1L, blogPost.getId(), request);

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
        BlogImageAsset uploadedImage = blogImageAssetRepository.save(
                createImage(blogPost.getId(), BlogImageAssetStatus.UPLOADED));

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                null,
                null,
                null,
                null,
                null,
                List.of(uploadedImage.getId())
        );

        // when
        blogDraftService.updatePost(1L, blogPost.getId(), request);

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
        BlogImageAsset attachedImage = blogImageAssetRepository.save(
                createImage(blogPost.getId(), BlogImageAssetStatus.ATTACHED));

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                null,
                null,
                null,
                null,
                null,
                List.of()
        );

        // when
        blogDraftService.updatePost(1L, blogPost.getId(), request);

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
        BlogImageAsset uploadedImage = blogImageAssetRepository.save(
                createImage(blogPost.getId(), BlogImageAssetStatus.UPLOADED));

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                null,
                null,
                null,
                null,
                null,
                List.of()
        );

        // when
        blogDraftService.updatePost(1L, blogPost.getId(), request);

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
        BlogImageAsset anotherPostImage = blogImageAssetRepository.save(
                createImage(anotherBlogPost.getId(), BlogImageAssetStatus.UPLOADED));

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                null,
                null,
                null,
                null,
                null,
                List.of(anotherPostImage.getId())
        );

        // when // then
        assertThatThrownBy(() -> blogDraftService.updatePost(1L, blogPost.getId(), request))
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
        assertThatThrownBy(() -> blogDraftService.updatePost(1L, blogPost.getId(), request))
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
        blogDraftService.updatePost(1L, blogPost.getId(), request);

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

    @Test
    void 발행_글도_수정_성공() {
        // given
        LocalDateTime publishedAt = LocalDateTime.of(2026, 3, 19, 21, 0, 0);
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("기존 제목")
                .content("기존 본문")
                .description("기존 설명")
                .status(BlogPostStatus.PUBLISHED)
                .visibility(BlogVisibility.PRIVATE)
                .publishedAt(publishedAt)
                .build());

        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                "수정된 제목",
                "수정된 본문",
                "수정된 설명",
                null,
                null,
                List.of()
        );

        // when
        blogDraftService.updatePost(1L, blogPost.getId(), request);

        // then
        BlogPost savedBlogPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedBlogPost.getTitle()).isEqualTo("수정된 제목");
            softly.assertThat(savedBlogPost.getContent()).isEqualTo("수정된 본문");
            softly.assertThat(savedBlogPost.getDescription()).isEqualTo("수정된 설명");
            softly.assertThat(savedBlogPost.getStatus()).isEqualTo(BlogPostStatus.PUBLISHED);
            softly.assertThat(savedBlogPost.getPublishedAt()).isEqualTo(publishedAt);
        });
    }

    @Test
    void 삭제된_글_수정_시_409() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DELETED, "삭제글"));
        UpdateBlogDraftRequest request = new UpdateBlogDraftRequest(
                "수정된 제목",
                "수정된 본문",
                "수정된 설명",
                null,
                null,
                List.of()
        );

        // when // then
        assertThatThrownBy(() -> blogDraftService.updatePost(1L, blogPost.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.RESOURCE_CONFLICT);
    }

    @Test
    void 내_DRAFT_글만_조회된다() {
        // given
        BlogPost myDraftPost = blogPostRepository.save(createDraftPost(1L));
        blogPostRepository.save(createDraftPost(2L));

        // when
        List<BlogDraftListItemResponse> responses = blogDraftService.getDrafts(1L);

        // then
        assertThat(responses).extracting(BlogDraftListItemResponse::postId)
                .containsExactly(myDraftPost.getId());
    }

    @Test
    void PUBLISHED_DELETED_글은_제외된다() {
        // given
        BlogPost draftPost = blogPostRepository.save(createDraftPost(1L));
        blogPostRepository.save(createBlogPost(1L, BlogPostStatus.PUBLISHED, "발행글"));
        blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DELETED, "삭제글"));

        // when
        List<BlogDraftListItemResponse> responses = blogDraftService.getDrafts(1L);

        // then
        assertThat(responses).extracting(BlogDraftListItemResponse::postId)
                .containsExactly(draftPost.getId());
    }

    @Test
    void updated_at_DESC_순으로_내려온다() {
        // given
        BlogPost olderDraftPost = blogPostRepository.save(createDraftPost(1L, "이전 제목"));
        BlogPost newerDraftPost = blogPostRepository.save(createDraftPost(1L, "최신 제목"));

        // when
        List<BlogDraftListItemResponse> responses = blogDraftService.getDrafts(1L);

        // then
        assertThat(responses).extracting(BlogDraftListItemResponse::postId)
                .containsExactly(newerDraftPost.getId(), olderDraftPost.getId());
    }

    @Test
    void title이_null인_초안도_조회된다() {
        // given
        BlogPost draftPost = blogPostRepository.save(createDraftPost(1L));

        // when
        List<BlogDraftListItemResponse> responses = blogDraftService.getDrafts(1L);

        // then
        assertThat(responses).singleElement()
                .satisfies(response -> {
                    assertThat(response.postId()).isEqualTo(draftPost.getId());
                    assertThat(response.title()).isNull();
                });
    }

    @Test
    void 내_DRAFT_글_수정용_상세_조회_성공() {
        // given
        BlogCategory blogCategory = blogCategoryRepository.save(BlogCategory.builder()
                .name("Backend")
                .build());
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("JPA 정리")
                .description("설명")
                .content("<p>본문</p>")
                .categoryId(blogCategory.getId())
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());
        BlogImageAsset thumbnailImage = blogImageAssetRepository.save(BlogImageAsset.builder()
                .blogPostId(blogPost.getId())
                .objectKey("blog/drafts/" + blogPost.getId() + "/thumbnail.png")
                .imageUrl("https://cdn.bombom.me/blog/10.png")
                .status(BlogImageAssetStatus.ATTACHED)
                .build());
        entityManager.createNativeQuery("""
                update blog_post
                set thumbnail_image_id = :thumbnailImageId
                where id = :postId
                """)
                .setParameter("thumbnailImageId", thumbnailImage.getId())
                .setParameter("postId", blogPost.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
        BlogImageAsset anotherAttachedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.ATTACHED));
        BlogHashtag spring = blogHashtagRepository.save(BlogHashtag.builder()
                .name("spring")
                .build());
        BlogHashtag jpa = blogHashtagRepository.save(BlogHashtag.builder()
                .name("jpa")
                .build());
        blogPostTagRepository.save(BlogPostTag.builder()
                .blogPostId(blogPost.getId())
                .blogHashtagId(jpa.getId())
                .build());
        blogPostTagRepository.save(BlogPostTag.builder()
                .blogPostId(blogPost.getId())
                .blogHashtagId(spring.getId())
                .build());

        // when
        BlogDraftDetailResponse response = blogDraftService.getDraft(1L, blogPost.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.postId()).isEqualTo(blogPost.getId());
            softly.assertThat(response.title()).isEqualTo("JPA 정리");
            softly.assertThat(response.description()).isEqualTo("설명");
            softly.assertThat(response.content()).isEqualTo("<p>본문</p>");
            softly.assertThat(response.status()).isEqualTo(BlogPostStatus.DRAFT);
            softly.assertThat(response.visibility()).isEqualTo(BlogVisibility.PRIVATE);
            softly.assertThat(response.thumbnailImage().imageId()).isEqualTo(thumbnailImage.getId());
            softly.assertThat(response.thumbnailImage().imageUrl()).isEqualTo("https://cdn.bombom.me/blog/10.png");
            softly.assertThat(response.category().id()).isEqualTo(blogCategory.getId());
            softly.assertThat(response.category().name()).isEqualTo("Backend");
            softly.assertThat(response.hashtags()).extracting("name").containsExactly("spring", "jpa");
            softly.assertThat(response.referenceImages()).extracting("imageId")
                    .containsExactly(thumbnailImage.getId(), anotherAttachedImage.getId());
            softly.assertThat(response.updatedAt()).isNotNull();
        });
    }

    @Test
    void 다른_사용자의_글_조회_시_403() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(2L));

        // when // then
        assertThatThrownBy(() -> blogDraftService.getDraft(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void 내_PUBLISHED_글_수정용_상세_조회_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.PUBLISHED, "발행글"));

        // when
        BlogDraftDetailResponse response = blogDraftService.getDraft(1L, blogPost.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.postId()).isEqualTo(blogPost.getId());
            softly.assertThat(response.title()).isEqualTo("발행글");
            softly.assertThat(response.status()).isEqualTo(BlogPostStatus.PUBLISHED);
        });
    }

    @Test
    void 삭제된_글_수정용_상세_조회_시_409() {
        // given
        BlogPost blogPost = blogPostRepository.save(createBlogPost(1L, BlogPostStatus.DELETED, "삭제글"));

        // when // then
        assertThatThrownBy(() -> blogDraftService.getDraft(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.RESOURCE_CONFLICT);
    }

    @Test
    void thumbnail_category가_null일_때_null로_응답한다() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("제목")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when
        BlogDraftDetailResponse response = blogDraftService.getDraft(1L, blogPost.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.thumbnailImage()).isNull();
            softly.assertThat(response.category()).isNull();
        });
    }

    @Test
    void hashtags가_없으면_빈_배열_응답() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));

        // when
        BlogDraftDetailResponse response = blogDraftService.getDraft(1L, blogPost.getId());

        // then
        assertThat(response.hashtags()).isEmpty();
    }

    @Test
    void referenceImages에는_ATTACHED만_포함되고_다른_상태는_제외된다() {
        // given
        BlogPost blogPost = blogPostRepository.save(createDraftPost(1L));
        BlogImageAsset attachedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.ATTACHED));
        blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.UPLOADED));
        blogImageAssetRepository.save(BlogImageAsset.builder()
                .blogPostId(blogPost.getId())
                .objectKey("blog/drafts/" + blogPost.getId() + "/delete-pending.png")
                .imageUrl("https://cdn.bombom.me/" + blogPost.getId() + "/delete-pending.png")
                .status(BlogImageAssetStatus.DELETE_PENDING)
                .deleteRequestedAt(LocalDateTime.of(2026, 3, 19, 21, 0, 0))
                .build());

        // when
        BlogDraftDetailResponse response = blogDraftService.getDraft(1L, blogPost.getId());

        // then
        assertThat(response.referenceImages()).singleElement()
                .satisfies(referenceImage -> assertThat(referenceImage.imageId()).isEqualTo(attachedImage.getId()));
    }

    @Test
    void DRAFT_글_발행_성공() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("제목")
                .content("<p>본문</p>")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when
        blogDraftService.publishDraft(1L, blogPost.getId());

        // then
        BlogPost publishedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(publishedPost.getStatus()).isEqualTo(BlogPostStatus.PUBLISHED);
    }

    @Test
    void 발행_시_status가_PUBLISHED로_바뀐다() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("제목")
                .content("본문")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when
        blogDraftService.publishDraft(1L, blogPost.getId());

        // then
        BlogPost publishedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(publishedPost.getStatus()).isEqualTo(BlogPostStatus.PUBLISHED);
    }

    @Test
    void 발행_시_published_at이_세팅된다() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("제목")
                .content("본문")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when
        blogDraftService.publishDraft(1L, blogPost.getId());

        // then
        BlogPost publishedPost = blogPostRepository.findById(blogPost.getId()).orElseThrow();
        assertThat(publishedPost.getPublishedAt()).isNotNull();
    }

    @Test
    void 발행_시_해당_post의_모든_image_asset이_ATTACHED로_바뀐다() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("제목")
                .content("본문")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());
        BlogImageAsset uploadedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.UPLOADED));
        BlogImageAsset attachedImage = blogImageAssetRepository.save(createImage(blogPost.getId(), BlogImageAssetStatus.ATTACHED));
        BlogImageAsset deletePendingImage = blogImageAssetRepository.save(BlogImageAsset.builder()
                .blogPostId(blogPost.getId())
                .objectKey("blog/drafts/" + blogPost.getId() + "/delete-pending.png")
                .imageUrl("https://cdn.bombom.me/" + blogPost.getId() + "/delete-pending.png")
                .status(BlogImageAssetStatus.DELETE_PENDING)
                .deleteRequestedAt(LocalDateTime.of(2026, 3, 19, 21, 0, 0))
                .build());

        // when
        blogDraftService.publishDraft(1L, blogPost.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(blogImageAssetRepository.findById(uploadedImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(BlogImageAssetStatus.ATTACHED);
            softly.assertThat(blogImageAssetRepository.findById(attachedImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(BlogImageAssetStatus.ATTACHED);
            softly.assertThat(blogImageAssetRepository.findById(deletePendingImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(BlogImageAssetStatus.ATTACHED);
            softly.assertThat(blogImageAssetRepository.findById(deletePendingImage.getId()).orElseThrow().getDeleteRequestedAt())
                    .isNull();
        });
    }

    @Test
    void 다른_사용자의_draft_발행_시_403() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(2L)
                .title("제목")
                .content("본문")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when // then
        assertThatThrownBy(() -> blogDraftService.publishDraft(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.FORBIDDEN_RESOURCE);
    }

    @Test
    void DRAFT_아닌_글_발행_시_409() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("제목")
                .content("본문")
                .status(BlogPostStatus.PUBLISHED)
                .visibility(BlogVisibility.PRIVATE)
                .publishedAt(LocalDateTime.of(2026, 3, 19, 21, 0, 0))
                .build());

        // when // then
        assertThatThrownBy(() -> blogDraftService.publishDraft(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.RESOURCE_CONFLICT);
    }

    @Test
    void title이_비어_있으면_400() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("   ")
                .content("본문")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when // then
        assertThatThrownBy(() -> blogDraftService.publishDraft(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    void content가_비어_있으면_400() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("제목")
                .content("   ")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when // then
        assertThatThrownBy(() -> blogDraftService.publishDraft(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    void thumbnail_image_id가_다른_글_이미지면_400() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("제목")
                .content("본문")
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());
        BlogPost anotherPost = blogPostRepository.save(createDraftPost(1L));
        BlogImageAsset anotherPostImage = blogImageAssetRepository.save(createImage(anotherPost.getId(), BlogImageAssetStatus.ATTACHED));

        entityManager.createNativeQuery("""
                update blog_post
                set thumbnail_image_id = :thumbnailImageId
                where id = :postId
                """)
                .setParameter("thumbnailImageId", anotherPostImage.getId())
                .setParameter("postId", blogPost.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // when // then
        assertThatThrownBy(() -> blogDraftService.publishDraft(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    void thumbnail_image_id가_존재하지_않으면_404() {
        // given
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(1L)
                .title("제목")
                .content("본문")
                .thumbnailImageId(999L)
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        // when // then
        assertThatThrownBy(() -> blogDraftService.publishDraft(1L, blogPost.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
    }

    private BlogPost createDraftPost(Long memberId) {
        return createBlogPost(memberId, BlogPostStatus.DRAFT, null);
    }

    private BlogPost createDraftPost(
            Long memberId,
            String title
    ) {
        return createBlogPost(memberId, BlogPostStatus.DRAFT, title);
    }

    private BlogPost createBlogPost(
            Long memberId,
            BlogPostStatus status,
            String title
    ) {
        return BlogPost.builder()
                .memberId(memberId)
                .title(title)
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
                .objectKey("blog/drafts/" + postId + "/" + status + ".png")
                .imageUrl("https://cdn.bombom.me/" + postId + "/" + status + ".png")
                .status(status)
                .build();
    }
}
