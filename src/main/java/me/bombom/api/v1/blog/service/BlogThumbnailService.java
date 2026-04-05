package me.bombom.api.v1.blog.service;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogImageAssetStatus;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogThumbnailService {

    private final Clock clock;
    private final BlogPostRepository blogPostRepository;
    private final BlogImageAssetRepository blogImageAssetRepository;

    @Transactional
    public void assignThumbnail(
            Long memberId,
            Long postId,
            Long imageId
    ) {
        String operation = "assignThumbnail";
        BlogPost blogPost = findBlogPost(postId, operation);
        validateOwner(blogPost, memberId, operation);
        validateActiveStatus(blogPost, operation);

        BlogImageAsset newThumbnailImage = findBlogImageAsset(imageId, operation);
        validateThumbnailImage(blogPost, newThumbnailImage);

        Long previousThumbnailImageId = blogPost.getThumbnailImageId();
        blogPost.assignThumbnailImage(imageId);
        newThumbnailImage.attach();

        if (previousThumbnailImageId == null || previousThumbnailImageId.equals(imageId)) {
            return;
        }

        adjustPreviousThumbnailImage(previousThumbnailImageId, operation);
    }

    @Transactional
    public void removeThumbnail(
            Long memberId,
            Long postId
    ) {
        String operation = "removeThumbnail";
        BlogPost blogPost = findBlogPost(postId, operation);
        validateOwner(blogPost, memberId, operation);
        validateActiveStatus(blogPost, operation);

        Long thumbnailImageId = blogPost.getThumbnailImageId();
        if (thumbnailImageId == null) {
            return;
        }

        blogPost.clearThumbnailImage();
        adjustPreviousThumbnailImage(thumbnailImageId, operation);
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

    private BlogImageAsset findBlogImageAsset(
            Long imageId,
            String operation
    ) {
        return blogImageAssetRepository.findById(imageId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "blogImageAsset")
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

    private void validateActiveStatus(
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

    private void validateThumbnailImage(
            BlogPost blogPost,
            BlogImageAsset blogImageAsset
    ) {
        boolean isForeignImage = blogImageAsset.getBlogPostId().equals(blogPost.getId()) == false;
        if (isForeignImage) {
            throw invalidInput("imageId");
        }

        boolean isDeletePendingImage = blogImageAsset.getStatus() == BlogImageAssetStatus.DELETE_PENDING;
        if (isDeletePendingImage) {
            throw invalidInput("imageId");
        }
    }

    private void adjustPreviousThumbnailImage(
            Long previousThumbnailImageId,
            String operation
    ) {
        BlogImageAsset previousThumbnailImage = findBlogImageAsset(previousThumbnailImageId, operation);
        previousThumbnailImage.markDeletePending(LocalDateTime.now(clock));
    }

    private CIllegalArgumentException invalidInput(String field) {
        return new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                .addContext("field", field);
    }
}
