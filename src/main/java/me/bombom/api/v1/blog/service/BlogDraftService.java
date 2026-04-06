package me.bombom.api.v1.blog.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogHashtag;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogImageAssetStatus;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogPostTag;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.dto.BlogDraftCategoryResponse;
import me.bombom.api.v1.blog.dto.BlogDraftDetailResponse;
import me.bombom.api.v1.blog.dto.BlogDraftHashtagResponse;
import me.bombom.api.v1.blog.dto.BlogDraftListItemResponse;
import me.bombom.api.v1.blog.dto.BlogDraftReferenceImageResponse;
import me.bombom.api.v1.blog.dto.BlogDraftThumbnailImageResponse;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.blog.dto.UpdateBlogDraftRequest;
import me.bombom.api.v1.blog.repository.BlogCategoryRepository;
import me.bombom.api.v1.blog.repository.BlogHashtagRepository;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.blog.repository.BlogPostTagRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogDraftService {

    private final Clock clock;
    private final BlogPostRepository blogPostRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final BlogHashtagRepository blogHashtagRepository;
    private final BlogPostTagRepository blogPostTagRepository;
    private final BlogImageAssetRepository blogImageAssetRepository;

    @Transactional
    public CreateBlogDraftResponse createDraft(Long memberId) {
        BlogPost blogPost = blogPostRepository.save(BlogPost.builder()
                .memberId(memberId)
                .status(BlogPostStatus.DRAFT)
                .visibility(BlogVisibility.PRIVATE)
                .build());

        return CreateBlogDraftResponse.from(blogPost);
    }

    @Transactional
    public void updatePost(Long memberId, Long postId, UpdateBlogDraftRequest request) {
        request.validate();

        String operation = "updatePost";
        BlogPost blogPost = findBlogPost(postId, operation);
        validateOwner(blogPost, memberId, operation);
        validateEditableStatus(blogPost, operation);

        validateCategory(request.categoryId());

        blogPost.updatePost(
                request.title(),
                request.content(),
                request.description(),
                request.categoryId()
        );

        replaceHashTags(postId, request.normalizedHashTags());
        updateReferencedImages(postId, request.distinctReferencedImageIds());
    }

    public List<BlogDraftListItemResponse> getDrafts(Long memberId) {
        return blogPostRepository.findAllDraftListItemByMemberIdAndStatus(
                memberId,
                BlogPostStatus.DRAFT
        );
    }

    public BlogDraftDetailResponse getPostForEdit(Long memberId, Long postId) {
        String operation = "getDraft";
        BlogPost blogPost = findBlogPost(postId, operation);
        validateOwner(blogPost, memberId, operation);
        validateEditableStatus(blogPost, operation);

        return BlogDraftDetailResponse.of(
                blogPost,
                getThumbnailImage(blogPost.getThumbnailImageId(), operation),
                getCategory(blogPost.getCategoryId(), operation),
                getHashTags(postId),
                getReferenceImages(postId)
        );
    }

    @Transactional
    public void publishDraft(Long memberId, Long postId) {
        String operation = "publishDraft";
        BlogPost blogPost = findBlogPost(postId, operation);
        validateOwner(blogPost, memberId, operation);
        validateDraftStatus(blogPost, operation);
        validatePublishableDraft(blogPost, operation);

        LocalDateTime publishedAt = LocalDateTime.now(clock);
        blogPost.publish(publishedAt);

        List<BlogImageAsset> blogImageAssets = blogImageAssetRepository.findAllByBlogPostId(postId);
        for (BlogImageAsset blogImageAsset : blogImageAssets) {
            blogImageAsset.attach();
        }
    }

    private BlogPost findBlogPost(
            Long postId,
            String operation
    ) {
        return blogPostRepository.findById(postId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "blogPost")
                        .addContext(ErrorContextKeys.OPERATION, operation));
    }

    private void validateOwner(
            BlogPost blogPost,
            Long memberId,
            String operation
    ) {
        if (blogPost.getMemberId().equals(memberId)) {
            return;
        }

        throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "blogPost")
                .addContext(ErrorContextKeys.OPERATION, operation);
    }

    private void validateDraftStatus(
            BlogPost blogPost,
            String operation
    ) {
        if (blogPost.getStatus() == BlogPostStatus.DRAFT) {
            return;
        }

        throw new CIllegalArgumentException(ErrorDetail.RESOURCE_CONFLICT)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "blogPost")
                .addContext(ErrorContextKeys.OPERATION, operation);
    }

    private void validateEditableStatus(
            BlogPost blogPost,
            String operation
    ) {
        BlogPostStatus status = blogPost.getStatus();
        if (status == BlogPostStatus.DRAFT || status == BlogPostStatus.PUBLISHED) {
            return;
        }

        throw new CIllegalArgumentException(ErrorDetail.RESOURCE_CONFLICT)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "blogPost")
                .addContext(ErrorContextKeys.OPERATION, operation);
    }

    private BlogDraftThumbnailImageResponse getThumbnailImage(
            Long thumbnailImageId,
            String operation
    ) {
        if (thumbnailImageId == null) {
            return null;
        }

        BlogImageAsset blogImageAsset = blogImageAssetRepository.findById(thumbnailImageId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "blogImageAsset")
                        .addContext(ErrorContextKeys.OPERATION, operation));

        return BlogDraftThumbnailImageResponse.from(blogImageAsset);
    }

    private BlogDraftCategoryResponse getCategory(
            Long categoryId,
            String operation
    ) {
        if (categoryId == null) {
            return null;
        }

        BlogCategory blogCategory = blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "blogCategory")
                        .addContext(ErrorContextKeys.OPERATION, operation));

        return BlogDraftCategoryResponse.from(blogCategory);
    }

    private List<BlogDraftHashtagResponse> getHashTags(Long postId) {
        List<Long> hashTagIds = blogPostTagRepository.findAllByBlogPostIdOrderByBlogHashtagId(postId).stream()
                .map(BlogPostTag::getBlogHashtagId)
                .toList();
        if (hashTagIds.isEmpty()) {
            return List.of();
        }

        return blogHashtagRepository.findAllByIdInOrderById(hashTagIds).stream()
                .map(BlogDraftHashtagResponse::from)
                .toList();
    }

    private List<BlogDraftReferenceImageResponse> getReferenceImages(Long postId) {
        return blogImageAssetRepository.findAllByBlogPostIdAndStatusOrderById(
                        postId,
                        BlogImageAssetStatus.ATTACHED
                ).stream()
                .map(BlogDraftReferenceImageResponse::from)
                .toList();
    }

    private void validateCategory(Long categoryId) {
        if (categoryId == null) {
            return;
        }

        blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "blogCategory")
                        .addContext("categoryId", categoryId));
    }

    private void validatePublishableDraft(
            BlogPost blogPost,
            String operation
    ) {
        validatePublishedTitle(blogPost.getTitle());
        validatePublishedContent(blogPost.getContent());
        validateThumbnailImage(blogPost, operation);
    }

    private void validatePublishedTitle(String title) {
        if (title != null && !title.isBlank()) {
            return;
        }

        throw invalidInput("title");
    }

    private void validatePublishedContent(String content) {
        if (content != null && !content.isBlank()) {
            return;
        }

        throw invalidInput("content");
    }

    private void validateThumbnailImage(
            BlogPost blogPost,
            String operation
    ) {
        Long thumbnailImageId = blogPost.getThumbnailImageId();
        if (thumbnailImageId == null) {
            return;
        }

        BlogImageAsset thumbnailImage = blogImageAssetRepository.findById(thumbnailImageId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "blogImageAsset")
                        .addContext(ErrorContextKeys.OPERATION, operation));

        boolean isForeignThumbnailImage = thumbnailImage.getBlogPostId().equals(blogPost.getId()) == false;
        if (isForeignThumbnailImage) {
            throw invalidInput("thumbnailImageId");
        }

        boolean isDeletePendingThumbnailImage = thumbnailImage.getStatus() == BlogImageAssetStatus.DELETE_PENDING;
        if (isDeletePendingThumbnailImage) {
            throw invalidInput("thumbnailImageId");
        }
    }

    private void replaceHashTags(Long postId, List<String> hashTags) {
        blogPostTagRepository.deleteAllByBlogPostId(postId);

        if (hashTags.isEmpty()) {
            return;
        }

        List<BlogHashtag> existingHashTags = blogHashtagRepository.findAllByNameIn(hashTags);
        Map<String, BlogHashtag> hashTagMap = new LinkedHashMap<>();
        for (BlogHashtag existingHashTag : existingHashTags) {
            hashTagMap.put(existingHashTag.getName(), existingHashTag);
        }

        List<BlogHashtag> newHashTags = hashTags.stream()
                .filter(tag -> !hashTagMap.containsKey(tag))
                .map(tag -> BlogHashtag.builder()
                        .name(tag)
                        .build())
                .toList();

        List<BlogHashtag> savedHashTags = blogHashtagRepository.saveAll(newHashTags);
        for (BlogHashtag savedHashTag : savedHashTags) {
            hashTagMap.put(savedHashTag.getName(), savedHashTag);
        }

        List<BlogPostTag> blogPostTags = hashTags.stream()
                .map(tag -> BlogPostTag.builder()
                        .blogPostId(postId)
                        .blogHashtagId(hashTagMap.get(tag).getId())
                        .build())
                .toList();

        blogPostTagRepository.saveAll(blogPostTags);
    }

    private void updateReferencedImages(Long postId, List<Long> referencedImageIds) {
        List<BlogImageAsset> blogImages = blogImageAssetRepository.findAllByBlogPostId(postId);
        Map<Long, BlogImageAsset> blogImageMap = new LinkedHashMap<>();
        for (BlogImageAsset blogImage : blogImages) {
            blogImageMap.put(blogImage.getId(), blogImage);
        }

        validateReferencedImages(postId, referencedImageIds, blogImageMap);

        for (Long referencedImageId : referencedImageIds) {
            blogImageMap.get(referencedImageId).attach();
        }

        LocalDateTime deleteRequestedAt = LocalDateTime.now(clock);
        for (BlogImageAsset blogImage : blogImages) {
            boolean isReferencedImage = referencedImageIds.contains(blogImage.getId());
            if (isReferencedImage) {
                continue;
            }

            boolean isAttachedImage = blogImage.getStatus() == BlogImageAssetStatus.ATTACHED;
            if (isAttachedImage) {
                blogImage.markDeletePending(deleteRequestedAt);
            }
        }
    }

    private void validateReferencedImages(
            Long postId,
            List<Long> referencedImageIds,
            Map<Long, BlogImageAsset> blogImageMap
    ) {
        if (referencedImageIds.isEmpty()) {
            return;
        }

        List<BlogImageAsset> referencedImages = blogImageAssetRepository.findAllByIdIn(referencedImageIds);
        if (referencedImages.size() != referencedImageIds.size()) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "blogImageAsset")
                    .addContext(ErrorContextKeys.OPERATION, "updatePost");
        }

        boolean containsForeignImage = referencedImages.stream()
                .anyMatch(image -> !image.getBlogPostId().equals(postId));

        if (containsForeignImage) {
            throw invalidInput("referencedImageIds");
        }

        boolean containsUnknownImage = referencedImageIds.stream()
                .anyMatch(imageId -> !blogImageMap.containsKey(imageId));

        if (containsUnknownImage) {
            throw invalidInput("referencedImageIds");
        }
    }

    private CIllegalArgumentException invalidInput(String field) {
        return new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                .addContext("field", field);
    }
}
