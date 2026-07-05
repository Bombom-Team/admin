package me.bombom.api.v1.blog.dto;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.blog.domain.BlogVisibility;

public record UpdateBlogPostVisibilityRequest(

        @NotNull
        BlogVisibility visibility
) {
}
