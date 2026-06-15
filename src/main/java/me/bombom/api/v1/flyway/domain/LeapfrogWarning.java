package me.bombom.api.v1.flyway.domain;

/**
 * 순서 역전 경고. {@code mine}(낮은 번호, 아직 미적용)이 {@code ahead}(높은 번호, 먼저 적용/머지) 뒤에
 * out-of-order로 끼어들 때 같은 테이블/컬럼을 건드리는 상황을 나타낸다.
 */
public record LeapfrogWarning(
        TrackedMigration mine,
        TrackedMigration ahead,
        ConflictSeverity severity
) {
}
