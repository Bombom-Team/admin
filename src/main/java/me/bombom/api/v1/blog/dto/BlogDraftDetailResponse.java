package me.bombom.api.v1.blog.dto;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogVisibility;

public record BlogDraftDetailResponse(
        Long postId,
        String title,
        String description,
        String content,
        BlogPostStatus status,
        BlogVisibility visibility,
        BlogDraftThumbnailImageResponse thumbnailImage,
        BlogDraftCategoryResponse category,
        List<BlogDraftHashtagResponse> hashtags,
        List<BlogDraftReferenceImageResponse> referenceImages,
        LocalDateTime updatedAt
) {

    public static BlogDraftDetailResponse of(
            BlogPost blogPost,
            BlogDraftThumbnailImageResponse thumbnailImage,
            BlogDraftCategoryResponse category,
            List<BlogDraftHashtagResponse> hashtags,
            List<BlogDraftReferenceImageResponse> referenceImages
    ) {
        return new BlogDraftDetailResponse(
                blogPost.getId(),
                blogPost.getTitle(),
                blogPost.getDescription(),
                blogPost.getContent(),
                blogPost.getStatus(),
                blogPost.getVisibility(),
                thumbnailImage,
                category,
                hashtags,
                referenceImages,
                blogPost.getUpdatedAt()
        );
    }
}
