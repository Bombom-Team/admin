package me.bombom.api.v1.blog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.blog.dto.AssignBlogPostThumbnailRequest;
import me.bombom.api.v1.blog.dto.BlogDraftDetailResponse;
import me.bombom.api.v1.blog.dto.BlogPostDetailResponse;
import me.bombom.api.v1.blog.dto.BlogPostListItemResponse;
import me.bombom.api.v1.blog.domain.BlogVisibility;
import me.bombom.api.v1.blog.dto.UpdateBlogDraftRequest;
import me.bombom.api.v1.blog.dto.UpdateBlogPostVisibilityRequest;
import me.bombom.api.v1.blog.dto.UploadBlogDraftImageResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Blog Post", description = "블로그 글 공통 관리 API")
@ApiResponses({
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface BlogPostControllerApi {

    @Operation(summary = "블로그 글 목록 조회", description = "작성자와 관계없이 블로그 글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<BlogPostListItemResponse> getPosts(@RequestParam(required = false) BlogVisibility visibility);

    @Operation(summary = "블로그 글 상세 조회", description = "블로그 글 상세 정보를 조회합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content)
    })
    BlogPostDetailResponse getPost(@PathVariable Long postId);

    @Operation(summary = "블로그 글 수정용 상세 조회", description = "수정 가능한 블로그 글(DRAFT, PUBLISHED) 상세 정보를 조회합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "수정할 수 없는 상태", content = @Content)
    })
    BlogDraftDetailResponse getPostForEdit(
            @LoginMember Member member,
            @PathVariable Long postId
    );

    @Operation(summary = "블로그 글 수정", description = "활성 블로그 글(DRAFT, PUBLISHED)을 수정합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "수정할 수 없는 상태", content = @Content)
    })
    void updatePost(
            @LoginMember Member member,
            @PathVariable Long postId,
            @Valid @RequestBody UpdateBlogDraftRequest request
    );

    @Operation(summary = "블로그 글 이미지 업로드", description = "활성 블로그 글(DRAFT, PUBLISHED)에 이미지를 업로드합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "201", description = "업로드 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
                    @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "업로드할 수 없는 상태", content = @Content)
    })
    UploadBlogDraftImageResponse uploadPostImage(
            @LoginMember Member member,
            @PathVariable Long postId,
            MultipartFile imageFile
    );

    @Operation(summary = "블로그 글 삭제", description = "활성 블로그 글(DRAFT, PUBLISHED)을 soft delete 합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "삭제할 수 없는 상태", content = @Content)
    })
    void deletePost(
            @LoginMember Member member,
            @PathVariable Long postId
    );

    @Operation(summary = "블로그 글 공개 범위 수정", description = "활성 블로그 글(DRAFT, PUBLISHED)의 공개 범위를 수정합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
                    @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "수정할 수 없는 상태", content = @Content)
    })
    void updatePostVisibility(
            @LoginMember Member member,
            @PathVariable Long postId,
            @Valid @RequestBody UpdateBlogPostVisibilityRequest request
    );

    @Operation(summary = "블로그 글 썸네일 등록", description = "활성 블로그 글(DRAFT, PUBLISHED)에 썸네일을 지정합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "204", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
                    @ApiResponse(responseCode = "404", description = "블로그 글 또는 이미지를 찾을 수 없음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "등록할 수 없는 상태", content = @Content)
    })
    void assignThumbnail(
            @LoginMember Member member,
            @PathVariable Long postId,
            @Valid @RequestBody AssignBlogPostThumbnailRequest request
    );

    @Operation(summary = "블로그 글 썸네일 제거", description = "활성 블로그 글(DRAFT, PUBLISHED)의 썸네일을 제거합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "204", description = "제거 성공"),
                    @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "제거할 수 없는 상태", content = @Content)
    })
    void removeThumbnail(
            @LoginMember Member member,
            @PathVariable Long postId
    );
}
