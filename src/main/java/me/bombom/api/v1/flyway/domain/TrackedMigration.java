package me.bombom.api.v1.flyway.domain;

/**
 * 형상 추적 대상이 되는 단일 마이그레이션. 적용완료/PR/이슈 등 모든 소스를 같은 모양으로 표현한다.
 */
public record TrackedMigration(
        MigrationVersion version,
        String description,
        MigrationStatus status,
        MigrationScript script
) {

    public boolean isPending() {
        return status.isPending();
    }

    public ConflictSeverity severityAgainst(TrackedMigration ahead) {
        if (script.sharesColumnWith(ahead.script)) {
            return ConflictSeverity.COLUMN;
        }
        if (script.sharesTableWith(ahead.script)) {
            return ConflictSeverity.TABLE;
        }

        return ConflictSeverity.NONE;
    }
}
