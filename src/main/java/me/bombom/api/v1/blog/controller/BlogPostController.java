package me.bombom.api.v1.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.dto.UpdateBlogDraftRequest;
import me.bombom.api.v1.blog.service.BlogDraftService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/blog/posts")
public class BlogPostController {

    private final BlogDraftService blogDraftService;

    @PutMapping("/{postId}")
    public void updatePost(
            @LoginMember Member member,
            @PathVariable Long postId,
            @Valid @RequestBody UpdateBlogDraftRequest request
    ) {
        blogDraftService.updatePost(member.getId(), postId, request);
    }
}
