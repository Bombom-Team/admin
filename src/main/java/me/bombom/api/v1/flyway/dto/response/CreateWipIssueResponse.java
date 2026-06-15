package me.bombom.api.v1.flyway.dto.response;

/**
 * 작업중 이슈 생성 결과.
 */
public record CreateWipIssueResponse(
        int issueNumber,
        String issueUrl
) {
}
