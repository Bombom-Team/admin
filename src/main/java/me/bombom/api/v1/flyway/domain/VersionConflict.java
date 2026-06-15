package me.bombom.api.v1.flyway.domain;

import java.util.List;

/**
 * 같은 버전 번호를 둘 이상이 사용하는 충돌. (Flyway 적용 시 중복 버전으로 실패하는 케이스)
 */
public record VersionConflict(
        MigrationVersion version,
        List<TrackedMigration> migrations
) {
}
