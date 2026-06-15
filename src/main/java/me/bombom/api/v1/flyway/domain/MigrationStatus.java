package me.bombom.api.v1.flyway.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 마이그레이션의 파이프라인 단계. 숫자가 클수록 운영(DB)에 가깝다.
 */
@Getter
@RequiredArgsConstructor
public enum MigrationStatus {

    LOCAL_WIP(0),
    PR_REVIEW(1),
    MERGE_PENDING(2),
    DB_APPLIED(3);

    private final int order;

    public boolean isAheadOf(MigrationStatus other) {
        return order > other.order;
    }

    public boolean isPending() {
        return order < DB_APPLIED.order;
    }
}
