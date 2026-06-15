package me.bombom.api.v1.flyway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.flyway.dto.request.CreateWipIssueRequest;
import me.bombom.api.v1.flyway.dto.response.CreateWipIssueResponse;
import me.bombom.api.v1.flyway.dto.response.FlywayOverviewResponse;
import me.bombom.api.v1.flyway.dto.response.MigrationScriptResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Flyway", description = "Flyway 버전 형상 조회 / 작업중 등록 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
})
public interface FlywayControllerApi {

    @Operation(summary = "형상 개요 조회",
            description = "적용완료/PR/이슈 통합 목록 + 같은번호 충돌 + 순서역전 경고 + 다음 안전 번호")
    FlywayOverviewResponse getOverview();

    @Operation(summary = "마이그레이션 스크립트 조회",
            description = "단일 마이그레이션 SQL 본문을 server→main 순으로 탐색해 반환")
    MigrationScriptResponse getScript(@RequestParam String fileName);

    @Operation(summary = "작업중 등록",
            description = "flyway-wip 라벨 이슈를 생성한다 (GITHUB_ISSUE_TOKEN 필요)")
    CreateWipIssueResponse createWipIssue(@Valid @RequestBody CreateWipIssueRequest request);
}
