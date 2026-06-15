package me.bombom.api.v1.flyway.dto.response;

import java.util.List;

/**
 * 순서 역전 경고. 낮은 번호(mine)가 먼저 적용된 높은 번호(ahead) 뒤에 끼어들며 겹치는 상황.
 * severity = TABLE(같은 테이블) / COLUMN(같은 컬럼).
 */
public record FlywayLeapfrogResponse(
        String mineVersion,
        String aheadVersion,
        List<String> sharedTables,
        String severity
) {
}
