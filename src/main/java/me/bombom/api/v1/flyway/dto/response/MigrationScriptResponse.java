package me.bombom.api.v1.flyway.dto.response;

/**
 * 단일 마이그레이션 SQL 본문 (행 클릭 시 lazy 로드).
 */
public record MigrationScriptResponse(
        String fileName,
        String content,
        String sourceUrl
) {
}
