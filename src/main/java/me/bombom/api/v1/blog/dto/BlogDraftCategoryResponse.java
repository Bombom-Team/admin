package me.bombom.api.v1.blog.dto;

import me.bombom.api.v1.blog.domain.BlogCategory;

public record BlogDraftCategoryResponse(
        Long id,
        String name
) {

    public static BlogDraftCategoryResponse from(BlogCategory blogCategory) {
        return new BlogDraftCategoryResponse(
                blogCategory.getId(),
                blogCategory.getName()
        );
    }
}
