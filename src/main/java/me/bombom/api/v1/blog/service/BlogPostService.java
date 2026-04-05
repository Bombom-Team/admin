package me.bombom.api.v1.blog.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogVisibility;
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
public class BlogPostService {

    private final Clock clock;
    private final BlogPostRepository blogPostRepository;
    private final BlogImageAssetRepository blogImageAssetRepository;

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
