package me.bombom.api.v1.blog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.blog.dto.BlogDraftDetailResponse;
import me.bombom.api.v1.blog.dto.BlogDraftListItemResponse;
import me.bombom.api.v1.blog.dto.CreateBlogDraftResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Blog Draft", description = "블로그 초안 관리 API")
@ApiResponses({
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface BlogDraftControllerApi {

    @Operation(summary = "블로그 초안 생성", description = "새로운 블로그 초안을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "초안 생성 성공")
    CreateBlogDraftResponse createDraft(@LoginMember Member member);

    @Operation(summary = "블로그 초안 목록 조회", description = "내 블로그 초안 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<BlogDraftListItemResponse> getDrafts(@LoginMember Member member);

    @Operation(summary = "블로그 초안 상세 조회", description = "내 블로그 초안 상세 정보를 조회합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "초안 상태가 아님", content = @Content)
    })
    BlogDraftDetailResponse getDraft(
            @LoginMember Member member,
            @PathVariable Long postId
    );

    @Operation(summary = "블로그 초안 발행", description = "블로그 초안을 발행합니다.")
    @ApiResponses({
                    @ApiResponse(responseCode = "201", description = "발행 성공"),
                    @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "발행할 수 없는 상태", content = @Content)
    })
    void publishDraft(
            @LoginMember Member member,
            @PathVariable Long postId
    );
}
