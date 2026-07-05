package me.bombom.api.v1.flyway.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.flyway.domain.ParsedWipIssue;
import me.bombom.api.v1.flyway.domain.WorkKind;
import me.bombom.api.v1.flyway.dto.request.CreateWipIssueRequest;
import org.junit.jupiter.api.Test;

class WipIssueTemplateTest {

    @Test
    void format_후_parse하면_기존테이블_값이_복원된다() {
        CreateWipIssueRequest request = new CreateWipIssueRequest(
                WorkKind.EXISTING_TABLE, "member", "V36.3.0", "add_grade_to_member", "mac");

        ParsedWipIssue parsed = WipIssueTemplate.parse(WipIssueTemplate.body(request));

        assertSoftly(softly -> {
            softly.assertThat(parsed.version()).isEqualTo("V36.3.0");
            softly.assertThat(parsed.table()).isEqualTo("member");
            softly.assertThat(parsed.newTable()).isFalse();
            softly.assertThat(parsed.description()).isEqualTo("add_grade_to_member");
        });
    }

    @Test
    void format_후_parse하면_새테이블은_대상테이블이_비어있다() {
        CreateWipIssueRequest request = new CreateWipIssueRequest(
                WorkKind.NEW_TABLE, null, "V37.0.0", "create_reading_streak_table", "ryan");

        ParsedWipIssue parsed = WipIssueTemplate.parse(WipIssueTemplate.body(request));

        assertSoftly(softly -> {
            softly.assertThat(parsed.newTable()).isTrue();
            softly.assertThat(parsed.table()).isEmpty();
            softly.assertThat(parsed.version()).isEqualTo("V37.0.0");
        });
    }
}
