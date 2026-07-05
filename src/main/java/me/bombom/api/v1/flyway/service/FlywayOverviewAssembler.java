package me.bombom.api.v1.flyway.service;

import java.util.Comparator;
import java.util.List;
import me.bombom.api.v1.flyway.config.FlywayMonitorProperties;
import me.bombom.api.v1.flyway.domain.LeapfrogWarning;
import me.bombom.api.v1.flyway.domain.MigrationStatus;
import me.bombom.api.v1.flyway.domain.MigrationVersion;
import me.bombom.api.v1.flyway.domain.OutOfOrderAnalyzer;
import me.bombom.api.v1.flyway.domain.ResolvedMigration;
import me.bombom.api.v1.flyway.domain.TrackedMigration;
import me.bombom.api.v1.flyway.domain.VersionConflict;
import me.bombom.api.v1.flyway.dto.response.FlywayConflictResponse;
import me.bombom.api.v1.flyway.dto.response.FlywayLeapfrogResponse;
import me.bombom.api.v1.flyway.dto.response.FlywayOverviewResponse;
import me.bombom.api.v1.flyway.dto.response.MigrationItemResponse;

/**
 * 분석 결과를 화면용 응답(FlywayOverviewResponse)으로 조립한다.
 */
final class FlywayOverviewAssembler {

    private FlywayOverviewAssembler() {
    }

    static FlywayOverviewResponse assemble(
            List<ResolvedMigration> resolved,
            OutOfOrderAnalyzer analyzer,
            FlywayMonitorProperties properties
    ) {
        List<TrackedMigration> tracked = trackedOf(resolved);
        List<MigrationVersion> versions = versionsOf(tracked);
        MigrationVersion latest = latestVersion(tracked);
        return new FlywayOverviewResponse(
                properties.getDeployBranch(),
                properties.getIntegrationBranch(),
                latest == null ? "" : latest.raw(),
                countApplied(tracked),
                countPending(tracked),
                nextSafeMinor(versions, latest),
                nextSafeMajor(versions),
                items(resolved),
                conflicts(resolved, analyzer.detectVersionConflicts(tracked), versions),
                leapfrogs(analyzer.detectLeapfrogs(tracked)));
    }

    static MigrationItemResponse toItem(ResolvedMigration resolved) {
        TrackedMigration migration = resolved.migration();
        return new MigrationItemResponse(
                migration.version().raw(),
                migration.description(),
                resolved.fileName(),
                migration.status().name(),
                migration.script().createsNewTable(),
                List.copyOf(migration.script().tables()),
                resolved.sourceLabel(),
                resolved.sourceUrl(),
                resolved.author());
    }

    private static List<MigrationItemResponse> items(List<ResolvedMigration> resolved) {
        return resolved.stream()
                .map(FlywayOverviewAssembler::toItem)
                .toList();
    }

    private static List<FlywayConflictResponse> conflicts(
            List<ResolvedMigration> resolved,
            List<VersionConflict> conflicts,
            List<MigrationVersion> versions
    ) {
        return conflicts.stream()
                .map(conflict -> toConflict(resolved, conflict, versions))
                .toList();
    }

    private static FlywayConflictResponse toConflict(
            List<ResolvedMigration> resolved,
            VersionConflict conflict,
            List<MigrationVersion> versions
    ) {
        List<String> sources = resolved.stream()
                .filter(migration -> migration.version().raw().equals(conflict.version().raw()))
                .map(ResolvedMigration::sourceLabel)
                .toList();
        return new FlywayConflictResponse(conflict.version().raw(), sources, suggest(conflict.version(), versions));
    }

    private static List<FlywayLeapfrogResponse> leapfrogs(List<LeapfrogWarning> warnings) {
        return warnings.stream()
                .map(FlywayOverviewAssembler::toLeapfrog)
                .toList();
    }

    private static FlywayLeapfrogResponse toLeapfrog(LeapfrogWarning warning) {
        List<String> sharedTables = warning.mine().script().tables().stream()
                .filter(warning.ahead().script().tables()::contains)
                .toList();
        return new FlywayLeapfrogResponse(
                warning.mine().version().raw(),
                warning.ahead().version().raw(),
                sharedTables,
                warning.severity().name());
    }

    private static List<TrackedMigration> trackedOf(List<ResolvedMigration> resolved) {
        return resolved.stream()
                .map(ResolvedMigration::migration)
                .toList();
    }

    private static List<MigrationVersion> versionsOf(List<TrackedMigration> tracked) {
        return tracked.stream()
                .map(TrackedMigration::version)
                .toList();
    }

    private static MigrationVersion latestVersion(List<TrackedMigration> tracked) {
        return tracked.stream()
                .filter(migration -> migration.status() == MigrationStatus.DB_APPLIED)
                .map(TrackedMigration::version)
                .max(Comparator.naturalOrder())
                .orElseGet(() -> highestOverall(tracked));
    }

    private static MigrationVersion highestOverall(List<TrackedMigration> tracked) {
        return tracked.stream()
                .map(TrackedMigration::version)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private static int countApplied(List<TrackedMigration> tracked) {
        return (int) tracked.stream()
                .filter(migration -> migration.status() == MigrationStatus.DB_APPLIED)
                .count();
    }

    private static int countPending(List<TrackedMigration> tracked) {
        return (int) tracked.stream()
                .filter(TrackedMigration::isPending)
                .count();
    }

    private static String nextSafeMinor(List<MigrationVersion> versions, MigrationVersion latest) {
        if (latest == null) {
            return "";
        }
        int major = latest.major();
        int maxMinor = maxMinorInMajor(versions, major);
        return "V" + major + "." + (maxMinor + 1) + ".0";
    }

    private static String nextSafeMajor(List<MigrationVersion> versions) {
        int maxMajor = versions.stream()
                .mapToInt(MigrationVersion::major)
                .max()
                .orElse(0);
        return "V" + (maxMajor + 1) + ".0.0";
    }

    private static String suggest(MigrationVersion version, List<MigrationVersion> versions) {
        int maxMinor = maxMinorInMajor(versions, version.major());
        return "V" + version.major() + "." + (maxMinor + 1) + ".0";
    }

    private static int maxMinorInMajor(List<MigrationVersion> versions, int major) {
        return versions.stream()
                .filter(version -> version.major() == major)
                .mapToInt(MigrationVersion::minor)
                .max()
                .orElse(0);
    }
}
