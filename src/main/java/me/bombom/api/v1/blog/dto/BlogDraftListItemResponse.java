package me.bombom.api.v1.blog.dto;

import java.time.LocalDateTime;

public record BlogDraftListItemResponse(
        Long postId,
        String title,
        LocalDateTime updatedAt
) {
}
