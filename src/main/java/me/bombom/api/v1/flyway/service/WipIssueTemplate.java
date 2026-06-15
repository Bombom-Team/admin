package me.bombom.api.v1.flyway.service;

import java.util.HashMap;
import java.util.Map;
import me.bombom.api.v1.flyway.domain.ParsedWipIssue;
import me.bombom.api.v1.flyway.domain.WorkKind;
import me.bombom.api.v1.flyway.dto.request.CreateWipIssueRequest;

/**
 * flyway-wip 이슈 본문을 생성/파싱한다. Issue Form 과 동일한 고정 양식을 사용해 라운드트립을 보장한다.
 */
public final class WipIssueTemplate {

    private static final String KIND = "작업 종류";
    private static final String TABLE = "대상 테이블";
    private static final String VERSION = "예정 버전";
    private static final String DESCRIPTION = "설명";
    private static final String ASSIGNEE = "담당자";
    private static final String NEW_TABLE_LABEL = "새로운 테이블";
    private static final String EXISTING_TABLE_LABEL = "기존 테이블";
    private static final String EMPTY_MARK = "-";

    private WipIssueTemplate() {
    }

    public static String title(CreateWipIssueRequest request) {
        return "[작업중] " + request.plannedVersion() + " " + request.description();
    }

    public static String body(CreateWipIssueRequest request) {
        String kindLabel = request.workKind() == WorkKind.NEW_TABLE ? NEW_TABLE_LABEL : EXISTING_TABLE_LABEL;
        String table = request.targetTable() == null ? EMPTY_MARK : request.targetTable();
        return section(KIND, kindLabel)
                + section(TABLE, table)
                + section(VERSION, request.plannedVersion())
                + section(DESCRIPTION, request.description())
                + section(ASSIGNEE, request.assignee());
    }

    public static ParsedWipIssue parse(String issueBody) {
        Map<String, String> sections = toSections(issueBody);
        boolean newTable = sections.getOrDefault(KIND, "").contains("새");
        return new ParsedWipIssue(
                sections.getOrDefault(VERSION, ""),
                normalizeTable(sections.getOrDefault(TABLE, "")),
                newTable,
                sections.getOrDefault(DESCRIPTION, ""));
    }

    private static String section(String heading, String value) {
        return "### " + heading + "\n" + value + "\n\n";
    }

    private static String normalizeTable(String value) {
        return EMPTY_MARK.equals(value) ? "" : value;
    }

    private static Map<String, String> toSections(String issueBody) {
        Map<String, String> sections = new HashMap<>();
        if (issueBody == null) {
            return sections;
        }
        String[] blocks = issueBody.split("###\\s*");
        for (String block : blocks) {
            putSection(sections, block);
        }

        return sections;
    }

    private static void putSection(Map<String, String> sections, String block) {
        String trimmed = block.strip();
        if (trimmed.isEmpty()) {
            return;
        }
        int lineBreak = trimmed.indexOf('\n');
        if (lineBreak > 0) {
            sections.put(trimmed.substring(0, lineBreak).strip(), trimmed.substring(lineBreak + 1).strip());
        }
    }
}
