package me.bombom.api.v1.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.dto.UpdateBlogDraftRequest;
import me.bombom.api.v1.blog.dto.UploadBlogDraftImageResponse;
import me.bombom.api.v1.blog.service.BlogDraftService;
import me.bombom.api.v1.blog.service.BlogImageService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/blog/posts")
public class BlogPostController {

    private final BlogDraftService blogDraftService;
    private final BlogImageService blogImageService;

    @PutMapping("/{postId}")
    public void updatePost(
            @LoginMember Member member,
            @PathVariable Long postId,
            @Valid @RequestBody UpdateBlogDraftRequest request
    ) {
        blogDraftService.updatePost(member.getId(), postId, request);
    }

    @PostMapping(value = "/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadBlogDraftImageResponse uploadPostImage(
            @LoginMember Member member,
            @PathVariable Long postId,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        return blogImageService.uploadPostImage(member.getId(), postId, imageFile);
    }
}
