package me.bombom.api.v1.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.notice.dto.CreateNoticeRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Notice", description = "공지사항 관리 API")
@ApiResponses({
                @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface NoticeControllerApi {

        @Operation(summary = "공지사항 생성", description = "새로운 공지사항 또는 이벤트를 등록합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "공지사항 생성 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
        })
        void createNotice(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "noticeCategory: [NOTICE, UPDATE, EVENT, CHECK] 중 하나 선택") @RequestBody CreateNoticeRequest request);
}
