package me.bombom.api.v1.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;

@Tag(name = "Dashboard", description = "대시보드 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface DashboardControllerApi {

    @Operation(summary = "대시보드 통계 조회", description = "전체 회원 수, 공지사항 수, 이번 달 신규 회원 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공")
    })
    DashboardStatsResponse getStats();
}
