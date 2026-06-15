package me.bombom.api.v1.flyway.domain;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flyway 마이그레이션 파일명을 파싱한 결과.
 * 예: {@code V31.7.0__add_source_to_newsletter.sql} → 버전 {@code V31.7.0}, 설명 {@code add_source_to_newsletter}.
 */
public record MigrationFile(
        MigrationVersion version,
        String description,
        String fileName
) {

    private static final Pattern FILE_PATTERN = Pattern.compile("(V[0-9.]+)__(.+)\\.sql");

    public static Optional<MigrationFile> parse(String fileName) {
        Matcher matcher = FILE_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return MigrationVersion.parse(matcher.group(1))
                    .map(version -> new MigrationFile(version, matcher.group(2), fileName));
        }

        return Optional.empty();
    }
}
