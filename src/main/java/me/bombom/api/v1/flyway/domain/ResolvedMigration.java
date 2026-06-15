package me.bombom.api.v1.flyway.domain;

/**
 * 분석 대상 마이그레이션 + 화면 표시용 소스 메타데이터(서버/PR/이슈 출처).
 */
public record ResolvedMigration(
        TrackedMigration migration,
        String fileName,
        String sourceLabel,
        String sourceUrl,
        String author
) {

    public MigrationVersion version() {
        return migration.version();
    }
}
