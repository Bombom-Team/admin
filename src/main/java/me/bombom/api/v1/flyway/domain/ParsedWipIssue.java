package me.bombom.api.v1.flyway.domain;

/**
 * flyway-wip 이슈 본문(Issue Form)에서 파싱한 작업중 정보.
 */
public record ParsedWipIssue(
        String version,
        String table,
        boolean newTable,
        String description
) {
}
