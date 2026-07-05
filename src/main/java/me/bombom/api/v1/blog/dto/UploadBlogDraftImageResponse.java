package me.bombom.api.v1.blog.dto;

import me.bombom.api.v1.blog.domain.BlogImageAsset;

public record UploadBlogDraftImageResponse(

        Long imageId,
        String imageUrl
) {

    public static UploadBlogDraftImageResponse from(BlogImageAsset blogImageAsset) {
        return new UploadBlogDraftImageResponse(blogImageAsset.getId(), blogImageAsset.getImageUrl());
    }
}
