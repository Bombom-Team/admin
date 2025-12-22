package me.bombom.api.v1.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.notice.dto.CreateNoticeRequest;
import me.bombom.api.v1.notice.dto.UpdateNoticeRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "Notice", description = "공지사항 관리 API")
@ApiResponses({
                @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface NoticeControllerApi {

        @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 조회합니다. (검색어, 카테고리 필터링 지원)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "목록 조회 성공")
        })
        Page<GetNoticeResponse> getNotices(
                        @ParameterObject @ModelAttribute GetNoticesRequest request,
                        @ParameterObject @PageableDefault Pageable pageable
        );

        @Operation(summary = "공지사항 생성", description = "새로운 공지사항 또는 이벤트를 등록합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "공지사항 생성 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
        })
        void createNotice(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "noticeCategory: [NOTICE, UPDATE, EVENT, CHECK] 중 하나 선택") @Valid @RequestBody CreateNoticeRequest request);

        @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "공지사항 수정 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 공지사항", content = @Content)
        })
        void updateNotice(
                        @Parameter(description = "수정할 공지사항 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
                        @RequestBody UpdateNoticeRequest request
        );

        @Operation(summary = "공지사항 삭제", description = "기존 공지사항을 삭제합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "공지사항 삭제 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
        })
        void deleteNotice(
                        @Parameter(description = "삭제할 공지사항 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id);
}
