package me.bombom.api.v1.newsletter.controller;

import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import me.bombom.api.v1.newsletter.dto.GetNewsletterResponse;
import me.bombom.api.v1.newsletter.dto.GetNewsletterSummaryResponse;
import me.bombom.api.v1.newsletter.dto.GetNewslettersRequest;
import me.bombom.api.v1.newsletter.dto.UpdateNewsletterRequest;
import me.bombom.api.v1.newsletter.dto.UpdateNewsletterStatusRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Newsletter", description = "뉴스레터 관리 API")
@ApiResponses({
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface NewsletterControllerApi {

        @Operation(summary = "뉴스레터 생성", description = "새로운 뉴스레터를 등록합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "뉴스레터 생성 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
        })
        void createNewsletter(@Valid @RequestBody CreateNewsletterRequest request);

        @Operation(summary = "뉴스레터 목록 조회", description = "뉴스레터 목록을 조회합니다. <br>"
                        + "- **검색**: 키워드로 이름, 설명, 발행 주기를 검색합니다. <br>"
                        + "- **필터**: 카테고리 이름으로 정확히 일치하는 뉴스레터를 필터링합니다. <br>"
                        + "- **정렬**: `LATEST`(최신순), `POPULAR`(인기순/구독자순) 정렬을 지원합니다.")
        @ApiResponse(responseCode = "200", description = "조회 성공")
        List<GetNewsletterSummaryResponse> getNewsletters(@ParameterObject GetNewslettersRequest request);

        @Operation(summary = "뉴스레터 상세 조회", description = "뉴스레터 상세 정보를 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "뉴스레터를 찾을 수 없음", content = @Content)
        })
        GetNewsletterResponse getNewsletterDetail(@PathVariable Long id);

        @Operation(summary = "뉴스레터 수정", description = "뉴스레터 정보를 수정합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "수정 성공"),
                        @ApiResponse(responseCode = "404", description = "뉴스레터를 찾을 수 없음", content = @Content)
        })
        void updateNewsletter(@PathVariable Long id, @RequestBody UpdateNewsletterRequest request);

        @Operation(summary = "뉴스레터 삭제", description = "뉴스레터를 삭제합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "삭제 성공"),
                        @ApiResponse(responseCode = "404", description = "뉴스레터를 찾을 수 없음", content = @Content)
        })
        void deleteNewsletter(@PathVariable Long id);

        @Operation(summary = "뉴스레터 발행 상태 변경",
                        description = "뉴스레터의 발행 상태를 변경합니다.<br><br>"
                                        + "**요청 status 값 (3종)**<br>"
                                        + "- **ACTIVE**: 발행중 (suspendedAt 자동 초기화)<br>"
                                        + "- **SUSPENDED**: 휴재 (suspendedAt 미입력 시 오늘 날짜)<br>"
                                        + "- **DISCONTINUED**: 폐간 (suspendedAt 미입력 시 오늘 날짜)<br><br>"
                                        + "**조회 응답 status 값 (4종)**<br>"
                                        + "- **ACTIVE**: 발행중<br>"
                                        + "- **SUSPENDED_VISIBLE**: 휴재 (본 서비스 노출 중)<br>"
                                        + "- **SUSPENDED_HIDDEN**: 휴재 (본 서비스 숨김)<br>"
                                        + "- **DISCONTINUED**: 폐간")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "상태 변경 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
                        @ApiResponse(responseCode = "404", description = "뉴스레터를 찾을 수 없음", content = @Content)
        })
        void updateStatus(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateNewsletterStatusRequest request
        );
}
