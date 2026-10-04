package me.bombom.api.v1.newsletterrequest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;
import me.bombom.api.v1.newsletterrequest.dto.request.RejectNewsletterRequestRequest;
import me.bombom.api.v1.newsletterrequest.dto.request.UpdateNewsletterRequestDraftRequest;
import me.bombom.api.v1.newsletterrequest.dto.response.ApproveNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestDetailResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestSummaryResponse;

@Tag(name = "NewsletterRequest", description = "뉴스레터 신청 검토 API")
@ApiResponses({
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface NewsletterRequestControllerApi {

    @Operation(summary = "뉴스레터 신청 목록 조회", description = "상태로 거를 수 있습니다. 최신 신청부터 보여줍니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<NewsletterRequestSummaryResponse> getNewsletterRequests(NewsletterRequestStatus status);

    @Operation(summary = "뉴스레터 신청 상세 조회", description = "신청, 자동 수집 초안, 추천 이유를 함께 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "신청을 찾을 수 없음", content = @Content)
    })
    NewsletterRequestDetailResponse getNewsletterRequest(Long id);

    @Operation(summary = "뉴스레터 신청 초안 수정", description = "보낸 필드만 수정합니다. 발신 이메일(email)은 여기서 입력합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "신청 또는 카테고리를 찾을 수 없음", content = @Content)
    })
    void updateDraft(Long id, UpdateNewsletterRequestDraftRequest request);

    @Operation(summary = "뉴스레터 신청 재수집", description = "초안을 수집 대기 상태로 되돌려 다음 주기에 다시 수집합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재수집 예약 성공"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 신청", content = @Content)
    })
    void recollect(Long id);

    @Operation(summary = "뉴스레터 신청 승인", description = "초안 값으로 뉴스레터를 등록하고 신청을 등록 완료로 바꿉니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "400", description = "초안에 필수 값이 비어 있음", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 처리된 신청", content = @Content)
    })
    ApproveNewsletterRequestResponse approve(Long id);

    @Operation(summary = "뉴스레터 신청 반려", description = "반려 사유를 남기고 신청을 반려합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 성공"),
            @ApiResponse(responseCode = "400", description = "반려 사유 누락", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 처리된 신청", content = @Content)
    })
    void reject(Long id, RejectNewsletterRequestRequest request);
}
