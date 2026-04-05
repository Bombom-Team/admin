package me.bombom.api.v1.blog.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.dto.BlogDraftDetailResponse;
import me.bombom.api.v1.blog.dto.BlogDraftListItemResponse;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.blog.service.BlogDraftService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/blog/drafts")
public class BlogDraftController {

    private final BlogDraftService blogDraftService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateBlogDraftResponse createDraft(@LoginMember Member member) {
        return blogDraftService.createDraft(member.getId());
    }

    @GetMapping
    public List<BlogDraftListItemResponse> getDrafts(@LoginMember Member member) {
        return blogDraftService.getDrafts(member.getId());
    }

    @GetMapping("/{postId}")
    public BlogDraftDetailResponse getDraft(
            @LoginMember Member member,
            @PathVariable Long postId
    ) {
        return blogDraftService.getDraft(member.getId(), postId);
    }

    @PostMapping("/{postId}/publish")
    @ResponseStatus(HttpStatus.CREATED)
    public void publishDraft(
            @LoginMember Member member,
            @PathVariable Long postId
    ) {
        blogDraftService.publishDraft(member.getId(), postId);
    }
}
