package me.bombom.api.v1.flyway.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.flyway.config.FlywayMonitorProperties;
import me.bombom.api.v1.flyway.domain.MigrationFile;
import me.bombom.api.v1.flyway.domain.MigrationScript;
import me.bombom.api.v1.flyway.domain.MigrationStatus;
import me.bombom.api.v1.flyway.domain.MigrationVersion;
import me.bombom.api.v1.flyway.domain.OutOfOrderAnalyzer;
import me.bombom.api.v1.flyway.domain.ParsedWipIssue;
import me.bombom.api.v1.flyway.domain.ResolvedMigration;
import me.bombom.api.v1.flyway.domain.TrackedMigration;
import me.bombom.api.v1.flyway.dto.request.CreateWipIssueRequest;
import me.bombom.api.v1.flyway.dto.response.CreateWipIssueResponse;
import me.bombom.api.v1.flyway.dto.response.FlywayOverviewResponse;
import me.bombom.api.v1.flyway.dto.response.MigrationScriptResponse;
import me.bombom.api.v1.flyway.github.CreateIssueCommand;
import me.bombom.api.v1.flyway.github.GitHubClient;
import me.bombom.api.v1.flyway.github.GitHubContentItem;
import me.bombom.api.v1.flyway.github.GitHubFileContent;
import me.bombom.api.v1.flyway.github.GitHubIssue;
import me.bombom.api.v1.flyway.github.GitHubPullFile;
import me.bombom.api.v1.flyway.github.GitHubPullRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlywayService {

    private final GitHubClient gitHubClient;
    private final FlywayMonitorProperties properties;
    private final OutOfOrderAnalyzer analyzer = new OutOfOrderAnalyzer();

    public FlywayOverviewResponse getOverview() {
        List<ResolvedMigration> resolved = enrichAheadScripts(collectAll());
        return FlywayOverviewAssembler.assemble(resolved, analyzer, properties);
    }

    public MigrationScriptResponse getScript(String fileName) {
        String content = readScriptContent(fileName, properties.getDeployBranch())
                .or(() -> readScriptContent(fileName, properties.getIntegrationBranch()))
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.OPERATION, "flywayGetScript")
                        .addContext("fileName", fileName));
        return new MigrationScriptResponse(fileName, content, blobUrl(properties.getDeployBranch(), fileName));
    }

    public CreateWipIssueResponse createWipIssue(CreateWipIssueRequest request) {
        validateTargetTable(request);
        if (properties.hasIssueToken() == false) {
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "flywayCreateWipIssue")
                    .addContext(ErrorContextKeys.REASON, "missingIssueToken");
        }
        CreateIssueCommand command = new CreateIssueCommand(
                WipIssueTemplate.title(request),
                WipIssueTemplate.body(request),
                List.of(properties.getWipLabel()));
        GitHubIssue created = gitHubClient.createIssue(properties.getIssueOwner(), properties.getIssueRepo(), command);
        return new CreateWipIssueResponse(created.number(), created.htmlUrl());
    }

    private List<ResolvedMigration> collectAll() {
        Set<String> serverFiles = migrationFileNames(properties.getDeployBranch());
        Set<String> mainFiles = migrationFileNames(properties.getIntegrationBranch());
        List<ResolvedMigration> all = new ArrayList<>();
        all.addAll(branchMigrations(serverFiles, MigrationStatus.DB_APPLIED, properties.getDeployBranch()));
        all.addAll(branchMigrations(notIn(mainFiles, serverFiles), MigrationStatus.MERGE_PENDING,
                properties.getIntegrationBranch()));
        List<ResolvedMigration> pullRequests = pullRequestMigrations();
        all.addAll(pullRequests);
        all.addAll(issueMigrations(versionKeys(pullRequests)));
        return all;
    }

    private Set<String> migrationFileNames(String branch) {
        Set<String> names = new LinkedHashSet<>();
        for (GitHubContentItem item : listMigrationDirectory(branch)) {
            if (item.isFile() && MigrationFile.parse(item.name()).isPresent()) {
                names.add(item.name());
            }
        }

        return names;
    }

    private List<GitHubContentItem> listMigrationDirectory(String branch) {
        return gitHubClient.listDirectory(
                properties.getSourceOwner(),
                properties.getSourceRepo(),
                properties.getMigrationPath(),
                branch);
    }

    private List<ResolvedMigration> branchMigrations(Set<String> fileNames, MigrationStatus status, String branch) {
        List<ResolvedMigration> migrations = new ArrayList<>();
        for (String fileName : fileNames) {
            MigrationFile.parse(fileName)
                    .map(file -> resolvedFromBranch(file, status, branch))
                    .ifPresent(migrations::add);
        }

        return migrations;
    }

    private ResolvedMigration resolvedFromBranch(MigrationFile file, MigrationStatus status, String branch) {
        TrackedMigration migration = new TrackedMigration(
                file.version(),
                file.description(),
                status,
                MigrationScript.analyze(""));
        return new ResolvedMigration(migration, file.fileName(), branch, blobUrl(branch, file.fileName()), "");
    }

    private List<ResolvedMigration> pullRequestMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<>();
        for (GitHubPullRequest pullRequest : gitHubClient.listOpenPullRequests(
                properties.getSourceOwner(), properties.getSourceRepo())) {
            migrations.addAll(migrationsFromPullRequest(pullRequest));
        }

        return migrations;
    }

    private List<ResolvedMigration> migrationsFromPullRequest(GitHubPullRequest pullRequest) {
        List<ResolvedMigration> migrations = new ArrayList<>();
        List<GitHubPullFile> files = gitHubClient.listPullFiles(
                properties.getSourceOwner(), properties.getSourceRepo(), pullRequest.number());
        for (GitHubPullFile file : files) {
            migrationFromPullFile(pullRequest, file).ifPresent(migrations::add);
        }

        return migrations;
    }

    private Optional<ResolvedMigration> migrationFromPullFile(GitHubPullRequest pullRequest, GitHubPullFile file) {
        if (isMigrationPath(file.filename()) == false) {
            return Optional.empty();
        }
        String fileName = baseName(file.filename());
        return MigrationFile.parse(fileName)
                .map(parsed -> pullRequestMigration(pullRequest, parsed, file));
    }

    private ResolvedMigration pullRequestMigration(
            GitHubPullRequest pullRequest,
            MigrationFile file,
            GitHubPullFile pullFile
    ) {
        TrackedMigration migration = new TrackedMigration(
                file.version(),
                file.description(),
                MigrationStatus.PR_REVIEW,
                MigrationScript.analyze(addedLines(pullFile.patch())));
        String label = "PR #" + pullRequest.number();
        return new ResolvedMigration(migration, file.fileName(), label, pullRequest.htmlUrl(),
                pullRequest.authorLogin());
    }

    private List<ResolvedMigration> issueMigrations(Set<String> takenVersions) {
        try {
            List<ResolvedMigration> migrations = new ArrayList<>();
            for (GitHubIssue issue : gitHubClient.listOpenIssuesWithLabel(
                    properties.getIssueOwner(), properties.getIssueRepo(), properties.getWipLabel())) {
                issueMigration(issue, takenVersions).ifPresent(migrations::add);
            }
            return migrations;
        } catch (CServerErrorException exception) {
            log.warn("flyway-wip 이슈 조회 실패, 로컬작업중 항목 생략: status={}", exception.getMessage());
            return List.of();
        }
    }

    private Optional<ResolvedMigration> issueMigration(GitHubIssue issue, Set<String> takenVersions) {
        if (issue.isPullRequest()) {
            return Optional.empty();
        }
        ParsedWipIssue parsed = WipIssueTemplate.parse(issue.body());
        return MigrationFile.parse(parsed.version() + "__placeholder.sql")
                .filter(file -> takenVersions.contains(file.version().raw()) == false)
                .map(file -> issueResolved(issue, parsed, file.version().raw()));
    }

    private ResolvedMigration issueResolved(GitHubIssue issue, ParsedWipIssue parsed, String version) {
        Set<String> tables = new LinkedHashSet<>();
        if (parsed.table().isBlank() == false) {
            tables.add(parsed.table());
        }
        TrackedMigration migration = new TrackedMigration(
                MigrationFile.parse(version + "__x.sql").orElseThrow().version(),
                parsed.description(),
                MigrationStatus.LOCAL_WIP,
                new MigrationScript(parsed.newTable(), tables, Set.of()));
        String fileName = version + "__" + parsed.description() + ".sql";
        return new ResolvedMigration(migration, fileName, "Issue #" + issue.number(), issue.htmlUrl(),
                issue.authorLogin());
    }

    private List<ResolvedMigration> enrichAheadScripts(List<ResolvedMigration> resolved) {
        Optional<MigrationVersion> minPending = minPendingVersion(resolved);
        if (minPending.isEmpty()) {
            return resolved;
        }
        Set<Integer> pendingMajors = pendingMajors(resolved);
        List<ResolvedMigration> enriched = new ArrayList<>();
        for (ResolvedMigration migration : resolved) {
            enriched.add(enrichIfAheadCandidate(migration, minPending.get(), pendingMajors));
        }

        return enriched;
    }

    private Set<Integer> pendingMajors(List<ResolvedMigration> resolved) {
        Set<Integer> majors = new LinkedHashSet<>();
        for (ResolvedMigration migration : resolved) {
            if (migration.migration().isPending()) {
                majors.add(migration.version().major());
            }
        }

        return majors;
    }

    private ResolvedMigration enrichIfAheadCandidate(
            ResolvedMigration migration,
            MigrationVersion minPending,
            Set<Integer> pendingMajors
    ) {
        boolean candidate = migration.migration().isPending() == false
                && migration.version().isHigherThan(minPending)
                && pendingMajors.contains(migration.version().major());
        if (candidate == false) {
            return migration;
        }
        String content = readScriptContent(migration.fileName(), migration.sourceLabel()).orElse("");
        TrackedMigration updated = new TrackedMigration(
                migration.version(),
                migration.migration().description(),
                migration.migration().status(),
                MigrationScript.analyze(content));
        return new ResolvedMigration(updated, migration.fileName(), migration.sourceLabel(),
                migration.sourceUrl(), migration.author());
    }

    private Optional<MigrationVersion> minPendingVersion(
            List<ResolvedMigration> resolved) {
        return resolved.stream()
                .filter(migration -> migration.migration().isPending())
                .map(ResolvedMigration::version)
                .min(MigrationVersion::compareTo);
    }

    private Optional<String> readScriptContent(String fileName, String branch) {
        GitHubFileContent content = gitHubClient.getFileContent(
                properties.getSourceOwner(),
                properties.getSourceRepo(),
                properties.getMigrationPath() + "/" + fileName,
                branch);
        if (content == null) {
            return Optional.empty();
        }

        return Optional.of(content.decode());
    }

    private void validateTargetTable(CreateWipIssueRequest request) {
        boolean missingTable = request.workKind().requiresTargetTable()
                && (request.targetTable() == null || request.targetTable().isBlank());
        if (missingTable) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.OPERATION, "flywayCreateWipIssue")
                    .addContext(ErrorContextKeys.REASON, "targetTableRequired");
        }
    }

    private Set<String> versionKeys(List<ResolvedMigration> migrations) {
        Set<String> versions = new LinkedHashSet<>();
        for (ResolvedMigration migration : migrations) {
            versions.add(migration.version().raw());
        }

        return versions;
    }

    private Set<String> notIn(Set<String> source, Set<String> exclude) {
        Set<String> result = new LinkedHashSet<>(source);
        result.removeAll(exclude);
        return result;
    }

    private boolean isMigrationPath(String path) {
        return path != null && path.startsWith(properties.getMigrationPath()) && path.endsWith(".sql");
    }

    private String baseName(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private String addedLines(String patch) {
        if (patch == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String line : patch.split("\n")) {
            appendAddedLine(builder, line);
        }

        return builder.toString();
    }

    private void appendAddedLine(StringBuilder builder, String line) {
        if (line.startsWith("+") && line.startsWith("+++") == false) {
            builder.append(line.substring(1))
                    .append('\n');
        }
    }

    private String blobUrl(String branch, String fileName) {
        return "https://github.com/" + properties.getSourceOwner() + "/" + properties.getSourceRepo()
                + "/blob/" + branch + "/" + properties.getMigrationPath() + "/" + fileName;
    }
}
