package me.bombom.api.v1.blog.dto;

import me.bombom.api.v1.blog.domain.BlogHashtag;

public record BlogDraftHashtagResponse(
        Long id,
        String name
) {

    public static BlogDraftHashtagResponse from(BlogHashtag blogHashtag) {
        return new BlogDraftHashtagResponse(
                blogHashtag.getId(),
                blogHashtag.getName()
        );
    }
}
