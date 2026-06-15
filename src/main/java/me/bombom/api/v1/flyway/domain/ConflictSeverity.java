package me.bombom.api.v1.flyway.domain;

/**
 * 순서 역전 경고의 강도.
 * {@code TABLE} = 같은 테이블만 겹침(약), {@code COLUMN} = 같은 컬럼까지 겹침(강).
 */
public enum ConflictSeverity {

    NONE,
    TABLE,
    COLUMN;

    public boolean isWarning() {
        return this == TABLE || this == COLUMN;
    }
}
