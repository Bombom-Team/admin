package me.bombom.api.v1.flyway.dto.response;

import java.util.List;

/**
 * Flyway 형상 뷰어의 메인 응답. 목록 + 충돌/역전 경고 + 다음 안전 번호를 한 번에 내려준다.
 */
public record FlywayOverviewResponse(
        String deployBranch,
        String integrationBranch,
        String latestVersion,
        int appliedCount,
        int pendingCount,
        String nextSafeMinor,
        String nextSafeMajor,
        List<MigrationItemResponse> migrations,
        List<FlywayConflictResponse> conflicts,
        List<FlywayLeapfrogResponse> leapfrogWarnings
) {
}
