package me.bombom.api.v1.flyway.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import me.bombom.api.v1.flyway.config.FlywayMonitorProperties;
import me.bombom.api.v1.flyway.dto.response.FlywayLeapfrogResponse;
import me.bombom.api.v1.flyway.dto.response.FlywayOverviewResponse;
import me.bombom.api.v1.flyway.github.GitHubClient;
import me.bombom.api.v1.flyway.github.GitHubContentItem;
import me.bombom.api.v1.flyway.github.GitHubFileContent;
import me.bombom.api.v1.flyway.github.GitHubPullFile;
import me.bombom.api.v1.flyway.github.GitHubPullRequest;
import me.bombom.api.v1.flyway.github.GitHubRef;
import me.bombom.api.v1.flyway.github.GitHubUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FlywayServiceTest {

    private static final String OWNER = "woowacourse-teams";
    private static final String REPO = "2025-bom-bom";
    private static final String PATH = "db/migration";

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private FlywayMonitorProperties properties;

    @InjectMocks
    private FlywayService flywayService;

    @BeforeEach
    void setUp() {
        given(properties.getSourceOwner()).willReturn(OWNER);
        given(properties.getSourceRepo()).willReturn(REPO);
        given(properties.getMigrationPath()).willReturn(PATH);
        given(properties.getDeployBranch()).willReturn("server");
        given(properties.getIntegrationBranch()).willReturn("main");
        given(properties.getIssueOwner()).willReturn("Bombom-Team");
        given(properties.getIssueRepo()).willReturn("admin");
        given(properties.getWipLabel()).willReturn("flyway-wip");
    }

    @Test
    void getOverview_낮은번호PR이_먼저적용된_높은번호와_같은컬럼이면_순서역전_강경고() {
        given(gitHubClient.listDirectory(OWNER, REPO, PATH, "server")).willReturn(List.of(
                file("V1.0.0__create_member.sql"),
                file("V36.2.0__restructure_member_grade.sql")));
        given(gitHubClient.listDirectory(OWNER, REPO, PATH, "main")).willReturn(List.of(
                file("V1.0.0__create_member.sql"),
                file("V36.2.0__restructure_member_grade.sql")));
        given(gitHubClient.listOpenPullRequests(OWNER, REPO)).willReturn(List.of(pullRequest()));
        given(gitHubClient.listPullFiles(OWNER, REPO, 842)).willReturn(List.of(new GitHubPullFile(
                "db/migration/V36.1.0__add_grade_to_member.sql",
                "added",
                "+ALTER TABLE member ADD COLUMN grade VARCHAR(10);")));
        given(gitHubClient.listOpenIssuesWithLabel("Bombom-Team", "admin", "flyway-wip")).willReturn(List.of());
        given(gitHubClient.getFileContent(OWNER, REPO, "db/migration/V36.2.0__restructure_member_grade.sql", "server"))
                .willReturn(base64("ALTER TABLE member MODIFY COLUMN grade INT;"));

        FlywayOverviewResponse overview = flywayService.getOverview();

        assertSoftly(softly -> {
            softly.assertThat(overview.latestVersion()).isEqualTo("V36.2.0");
            softly.assertThat(overview.appliedCount()).isEqualTo(2);
            softly.assertThat(overview.pendingCount()).isEqualTo(1);
            softly.assertThat(overview.leapfrogWarnings()).hasSize(1);
        });
        FlywayLeapfrogResponse warning = overview.leapfrogWarnings().get(0);
        assertSoftly(softly -> {
            softly.assertThat(warning.mineVersion()).isEqualTo("V36.1.0");
            softly.assertThat(warning.aheadVersion()).isEqualTo("V36.2.0");
            softly.assertThat(warning.severity()).isEqualTo("COLUMN");
            softly.assertThat(warning.sharedTables()).contains("member");
        });
    }

    private GitHubContentItem file(String name) {
        return new GitHubContentItem(name, PATH + "/" + name, "file", "sha");
    }

    private GitHubPullRequest pullRequest() {
        return new GitHubPullRequest(
                842,
                "feat: 회원 등급 컬럼 추가",
                "https://github.com/pr/842",
                new GitHubUser("mac"),
                new GitHubRef("BOM-1101"));
    }

    private GitHubFileContent base64(String sql) {
        String encoded = Base64.getEncoder()
                .encodeToString(sql.getBytes(StandardCharsets.UTF_8));
        return new GitHubFileContent(encoded, "base64");
    }
}
