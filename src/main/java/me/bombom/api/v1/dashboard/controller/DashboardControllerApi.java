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

    @Operation(summary = "대시보드 통계 조회", description = """
            회원, 가입 및 오늘 활동 집계에서 role_id=4인 테스트 계정을 제외합니다.
            일별 가입 추이는 서울 날짜 기준 오늘 포함 30일이며, 가입자가 없는 날은 0을 반환합니다.
            서버 인스턴스에서 운영/개발 DB와 서울 날짜별로 최대 3시간 캐시하며 aggregatedAt은 집계 완료 시각입니다.
            가입 집계는 현재 회원 데이터 기준으로 탈퇴한 회원은 포함하지 않습니다.
            탈퇴 집계에는 권한 정보가 없어 테스트 계정 제외를 적용하지 않습니다.
            오늘 활동은 기존 유효 세션 기준 집계이며 회원 연결이 없는 세션은 기존대로 포함합니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공")
    })
    DashboardStatsResponse getStats();
}
