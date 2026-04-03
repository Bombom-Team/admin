package me.bombom.api.v1.blog.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.dto.BlogDraftDetailResponse;
import me.bombom.api.v1.blog.dto.BlogDraftListItemResponse;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.blog.dto.UpdateBlogDraftRequest;
import me.bombom.api.v1.blog.dto.UploadBlogDraftImageResponse;
import me.bombom.api.v1.blog.service.BlogDraftService;
import me.bombom.api.v1.blog.service.BlogImageService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/blog/drafts")
public class BlogDraftController {

    private final BlogDraftService blogDraftService;
    private final BlogImageService blogImageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateBlogDraftResponse createDraft(@LoginMember Member member) {
        return blogDraftService.createDraft(member.getId());
    }

    @PostMapping(value = "/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadBlogDraftImageResponse uploadDraftImage(
            @LoginMember Member member,
            @PathVariable Long postId,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        return blogImageService.uploadDraftImage(member.getId(), postId, imageFile);
    }

    @PutMapping("/{postId}")
    public void updateDraft(
            @LoginMember Member member,
            @PathVariable Long postId,
            @Valid @RequestBody UpdateBlogDraftRequest request
    ) {
        blogDraftService.updateDraft(member.getId(), postId, request);
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
