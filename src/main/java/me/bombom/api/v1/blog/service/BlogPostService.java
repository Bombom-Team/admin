package me.bombom.api.v1.blog.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogHashtag;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
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
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogPostService {

    private final Clock clock;
    private final BlogPostRepository blogPostRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final BlogHashtagRepository blogHashtagRepository;
    private final BlogPostTagRepository blogPostTagRepository;
    private final BlogImageAssetRepository blogImageAssetRepository;

    public List<BlogPostListItemResponse> getPosts(
            Long memberId,
            BlogVisibility visibility
    ) {
        return blogPostRepository.findAllPostListItems(memberId, visibility);
    }

    public BlogPostDetailResponse getPost(Long postId) {
        String operation = "getPost";
        BlogPost blogPost = findBlogPost(postId, operation);

        return BlogPostDetailResponse.of(
                blogPost,
                getThumbnailImageUrl(blogPost.getThumbnailImageId(), operation),
                getCategoryName(blogPost.getCategoryId(), operation),
                getHashTags(postId)
        );
    }

    @Transactional
    public void deletePost(
            Long memberId,
            Long postId
    ) {
        String operation = "deletePost";
        BlogPost blogPost = findBlogPost(postId, operation);
        validateOwner(blogPost, memberId, operation);
        validateActiveStatus(blogPost, operation);

        blogPost.delete();

        LocalDateTime deleteRequestedAt = LocalDateTime.now(clock);
        List<BlogImageAsset> blogImageAssets = blogImageAssetRepository.findAllByBlogPostId(postId);
        for (BlogImageAsset blogImageAsset : blogImageAssets) {
            blogImageAsset.markDeletePending(deleteRequestedAt);
        }
    }

    @Transactional
    public void updatePostVisibility(
            Long memberId,
            Long postId,
            BlogVisibility visibility
    ) {
        String operation = "updatePostVisibility";
        BlogPost blogPost = findBlogPost(postId, operation);
        validateOwner(blogPost, memberId, operation);
        validateActiveStatus(blogPost, operation);

        blogPost.updateVisibility(visibility);
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

    private String getThumbnailImageUrl(
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

        return blogImageAsset.getImageUrl();
    }

    private String getCategoryName(
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

        return blogCategory.getName();
    }

    private List<String> getHashTags(Long postId) {
        List<Long> hashTagIds = blogPostTagRepository.findAllByBlogPostIdOrderByBlogHashtagId(postId).stream()
                .map(BlogPostTag::getBlogHashtagId)
                .toList();
        if (hashTagIds.isEmpty()) {
            return List.of();
        }

        return blogHashtagRepository.findAllByIdInOrderById(hashTagIds).stream()
                .map(BlogHashtag::getName)
                .toList();
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
}
