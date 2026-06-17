package me.bombom.api.v1.flyway.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Flyway 형상 뷰어 설정. application.yml 없이 동작하도록 기본값을 코드에 둔다.
 * - 마이그레이션/PR 조회 대상: woowacourse-teams/2025-bom-bom (public, 읽기)
 * - 작업중 이슈 생성 대상: Bombom-Team/admin (GITHUB_ISSUE_TOKEN 필요)
 */
@Getter
@Component
public class FlywayMonitorProperties {

    @Value("${flyway.source.owner:woowacourse-teams}")
    private String sourceOwner;

    @Value("${flyway.source.repo:2025-bom-bom}")
    private String sourceRepo;

    @Value("${flyway.source.deploy-branch:main}")
    private String deployBranch;

    @Value("${flyway.source.integration-branch:server-dev}")
    private String integrationBranch;

    @Value("${flyway.source.migration-path:backend/bom-bom-server/src/main/resources/db/migration}")
    private String migrationPath;

    @Value("${flyway.issue.owner:Bombom-Team}")
    private String issueOwner;

    @Value("${flyway.issue.repo:admin}")
    private String issueRepo;

    @Value("${flyway.issue.label:flyway-wip}")
    private String wipLabel;

    @Value("${GITHUB_READ_TOKEN:}")
    private String readToken;

    @Value("${GITHUB_ISSUE_TOKEN:}")
    private String issueToken;

    public boolean hasReadToken() {
        return readToken != null && readToken.isBlank() == false;
    }

    public boolean hasIssueToken() {
        return issueToken != null && issueToken.isBlank() == false;
    }
}
