package me.bombom.api.v1.blog.dto;

import java.time.LocalDateTime;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogVisibility;

public record BlogPostListItemResponse(
        Long postId,
        Long memberId,
        boolean isAuthor,
        String title,
        String description,
        BlogPostStatus status,
        BlogVisibility visibility,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt
) {
}
