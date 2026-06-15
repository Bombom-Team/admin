package me.bombom.api.v1.flyway.domain;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 마이그레이션 SQL 본문을 정적 분석해 영향 테이블/컬럼과 신규 테이블 여부를 추출한다.
 * out-of-order 충돌 판정(같은 테이블/컬럼 동시 변경)을 위한 휴리스틱이며, 완전한 SQL 파서는 아니다.
 */
public record MigrationScript(
        boolean createsNewTable,
        Set<String> tables,
        Set<String> columns
) {

    private static final Pattern CREATE_TABLE = Pattern.compile(
            "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?[`\"]?(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_REFERENCE = Pattern.compile(
            "(?:ALTER\\s+TABLE|INSERT\\s+INTO|UPDATE|DELETE\\s+FROM|DROP\\s+TABLE|TRUNCATE\\s+TABLE)\\s+[`\"]?(\\w+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern INDEX_TARGET = Pattern.compile(
            "CREATE\\s+(?:UNIQUE\\s+)?INDEX\\s+\\w+\\s+ON\\s+[`\"]?(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLUMN_CHANGE = Pattern.compile(
            "(?:ADD|DROP|MODIFY|CHANGE)\\s+(?:COLUMN\\s+)?[`\"]?(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Set<String> COLUMN_KEYWORDS = Set.of(
            "constraint", "primary", "unique", "index", "key", "foreign", "check", "column");

    public static MigrationScript analyze(String sql) {
        String safeSql = sql == null ? "" : sql;
        Set<String> createdTables = collect(CREATE_TABLE, safeSql);
        Set<String> tables = mergeTables(createdTables, safeSql);
        return new MigrationScript(createdTables.isEmpty() == false, tables, collectColumns(safeSql));
    }

    public boolean sharesTableWith(MigrationScript other) {
        return tables.stream()
                .anyMatch(other.tables::contains);
    }

    public boolean sharesColumnWith(MigrationScript other) {
        return columns.stream()
                .anyMatch(other.columns::contains);
    }

    private static Set<String> mergeTables(Set<String> createdTables, String sql) {
        Set<String> tables = newCaseInsensitiveSet();
        tables.addAll(createdTables);
        tables.addAll(collect(TABLE_REFERENCE, sql));
        tables.addAll(collect(INDEX_TARGET, sql));
        return tables;
    }

    private static Set<String> collectColumns(String sql) {
        Set<String> columns = newCaseInsensitiveSet();
        Matcher matcher = COLUMN_CHANGE.matcher(sql);
        while (matcher.find()) {
            String candidate = matcher.group(1).toLowerCase(Locale.ROOT);
            if (COLUMN_KEYWORDS.contains(candidate) == false) {
                columns.add(candidate);
            }
        }

        return columns;
    }

    private static Set<String> collect(Pattern pattern, String sql) {
        Set<String> values = newCaseInsensitiveSet();
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            values.add(matcher.group(1));
        }

        return values;
    }

    private static Set<String> newCaseInsensitiveSet() {
        return new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    }
}
