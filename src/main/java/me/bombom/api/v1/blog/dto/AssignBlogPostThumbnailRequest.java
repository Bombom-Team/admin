package me.bombom.api.v1.blog.dto;

import jakarta.validation.constraints.NotNull;

public record AssignBlogPostThumbnailRequest(

        @NotNull
        Long imageId
) {
}
