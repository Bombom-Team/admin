package me.bombom.api.v1.blog.dto;

import me.bombom.api.v1.blog.domain.BlogImageAsset;

public record BlogDraftReferenceImageResponse(
        Long imageId,
        String imageUrl
) {

    public static BlogDraftReferenceImageResponse from(BlogImageAsset blogImageAsset) {
        return new BlogDraftReferenceImageResponse(
                blogImageAsset.getId(),
                blogImageAsset.getImageUrl()
        );
    }
}
