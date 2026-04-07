package me.bombom.api.v1.blog.dto;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.blog.domain.BlogPost;

public record BlogPostDetailResponse(
        String title,
        String description,
        String content,
        String thumbnailImageUrl,
        String categoryName,
        LocalDateTime publishedAt,
        List<String> hashtags
) {

    public static BlogPostDetailResponse of(
            BlogPost blogPost,
            String thumbnailImageUrl,
            String categoryName,
            List<String> hashtags
    ) {
        return new BlogPostDetailResponse(
                blogPost.getTitle(),
                blogPost.getDescription(),
                blogPost.getContent(),
                thumbnailImageUrl,
                categoryName,
                blogPost.getPublishedAt(),
                hashtags
        );
    }
}
