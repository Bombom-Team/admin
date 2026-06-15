package me.bombom.api.v1.flyway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;

class MigrationScriptTest {

    @Test
    void analyze_create_table면_신규테이블로_보고_테이블을_담는다() {
        MigrationScript script = MigrationScript.analyze(
                "CREATE TABLE reading_streak (id BIGINT NOT NULL PRIMARY KEY);");

        assertSoftly(softly -> {
            softly.assertThat(script.createsNewTable()).isTrue();
            softly.assertThat(script.tables()).contains("reading_streak");
        });
    }

    @Test
    void analyze_alter_add_column이면_기존테이블과_컬럼을_추출한다() {
        MigrationScript script = MigrationScript.analyze(
                "ALTER TABLE newsletter ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'EXTERNAL';");

        assertSoftly(softly -> {
            softly.assertThat(script.createsNewTable()).isFalse();
            softly.assertThat(script.tables()).contains("newsletter");
            softly.assertThat(script.columns()).contains("source");
        });
    }

    @Test
    void analyze_제약조건_키워드는_컬럼으로_보지_않는다() {
        MigrationScript script = MigrationScript.analyze(
                "ALTER TABLE member ADD CONSTRAINT uk_member UNIQUE (email), ADD PRIMARY KEY (id);");

        assertThat(script.columns()).doesNotContain("constraint", "primary", "unique", "key");
    }

    @Test
    void analyze_create_index는_대상_테이블을_담는다() {
        MigrationScript script = MigrationScript.analyze(
                "CREATE INDEX idx_nd_member ON newsletter_detail (member_id);");

        assertThat(script.tables()).contains("newsletter_detail");
    }

    @Test
    void sharesTableWith_같은_테이블을_건드리면_참이다() {
        MigrationScript mine = MigrationScript.analyze("ALTER TABLE member ADD COLUMN grade VARCHAR(10);");
        MigrationScript ahead = MigrationScript.analyze("ALTER TABLE member DROP COLUMN grade_legacy;");
        MigrationScript unrelated = MigrationScript.analyze("ALTER TABLE member_contact ADD COLUMN phone VARCHAR(20);");

        assertSoftly(softly -> {
            softly.assertThat(mine.sharesTableWith(ahead)).isTrue();
            softly.assertThat(mine.sharesTableWith(unrelated)).isFalse();
        });
    }

    @Test
    void sharesColumnWith_같은_컬럼을_건드릴_때만_참이다() {
        MigrationScript mine = MigrationScript.analyze("ALTER TABLE member ADD COLUMN grade VARCHAR(10);");
        MigrationScript sameColumn = MigrationScript.analyze("ALTER TABLE member MODIFY COLUMN grade INT;");
        MigrationScript otherColumn = MigrationScript.analyze("ALTER TABLE member ADD COLUMN nickname VARCHAR(30);");

        assertSoftly(softly -> {
            softly.assertThat(mine.sharesColumnWith(sameColumn)).isTrue();
            softly.assertThat(mine.sharesColumnWith(otherColumn)).isFalse();
        });
    }
}
