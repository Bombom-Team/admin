package me.bombom.api.v1.blog.dto;

import me.bombom.api.v1.blog.domain.BlogImageAsset;

public record BlogDraftThumbnailImageResponse(
        Long imageId,
        String imageUrl
) {

    public static BlogDraftThumbnailImageResponse from(BlogImageAsset blogImageAsset) {
        return new BlogDraftThumbnailImageResponse(
                blogImageAsset.getId(),
                blogImageAsset.getImageUrl()
        );
    }
}
