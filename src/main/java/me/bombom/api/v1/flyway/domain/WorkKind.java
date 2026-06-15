package me.bombom.api.v1.flyway.domain;

/**
 * 작업중 등록 종류. 새 테이블은 대상 테이블 선택이 없고, 기존 테이블은 대상 테이블이 필요하다.
 */
public enum WorkKind {

    NEW_TABLE,
    EXISTING_TABLE;

    public boolean requiresTargetTable() {
        return this == EXISTING_TABLE;
    }
}
