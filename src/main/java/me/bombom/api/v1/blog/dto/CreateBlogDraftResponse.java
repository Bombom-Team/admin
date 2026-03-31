package me.bombom.api.v1.blog.dto;

import me.bombom.api.v1.blog.domain.BlogPost;

public record CreateBlogDraftResponse(

        Long postId
) {

    public static CreateBlogDraftResponse from(BlogPost blogPost) {
        return new CreateBlogDraftResponse(blogPost.getId());
    }
}
