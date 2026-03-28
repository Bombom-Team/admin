package me.bombom.api.v1.blog.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.blog.service.BlogDraftService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
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
}
