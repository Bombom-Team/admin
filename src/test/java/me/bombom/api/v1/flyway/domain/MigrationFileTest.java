package me.bombom.api.v1.flyway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class MigrationFileTest {

    @Test
    void parse_정상_파일명이면_버전과_설명을_추출한다() {
        Optional<MigrationFile> parsed = MigrationFile.parse("V31.7.0__add_source_to_newsletter.sql");

        assertThat(parsed).isPresent();
        assertSoftly(softly -> {
            softly.assertThat(parsed.get().version().raw()).isEqualTo("V31.7.0");
            softly.assertThat(parsed.get().description()).isEqualTo("add_source_to_newsletter");
            softly.assertThat(parsed.get().fileName()).isEqualTo("V31.7.0__add_source_to_newsletter.sql");
        });
    }

    @Test
    void parse_sql_파일이_아니면_빈값을_반환한다() {
        assertThat(MigrationFile.parse("README.md")).isEmpty();
        assertThat(MigrationFile.parse("V31.7.0_no_double_underscore.sql")).isEmpty();
    }
}
