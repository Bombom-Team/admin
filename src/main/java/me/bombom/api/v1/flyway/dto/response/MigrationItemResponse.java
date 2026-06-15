package me.bombom.api.v1.flyway.dto.response;

import java.util.List;

/**
 * 형상 목록의 한 행. 적용완료/PR/이슈 모든 소스를 같은 모양으로 표현한다.
 */
public record MigrationItemResponse(
        String version,
        String description,
        String fileName,
        String status,
        boolean createsNewTable,
        List<String> tables,
        String sourceLabel,
        String sourceUrl,
        String author
) {
}
