package me.bombom.api.v1.flyway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class MigrationVersionTest {

    @Test
    void parse_점으로_구분된_버전이면_숫자_파트로_분해한다() {
        Optional<MigrationVersion> parsed = MigrationVersion.parse("V31.7.0");

        assertThat(parsed).isPresent();
        assertThat(parsed.get().parts()).containsExactly(31, 7, 0);
    }

    @Test
    void parse_형식이_아니면_빈값을_반환한다() {
        assertThat(MigrationVersion.parse("31.7.0")).isEmpty();
        assertThat(MigrationVersion.parse("Vabc")).isEmpty();
    }

    @Test
    void compareTo_문자열이_아닌_숫자기준으로_정렬한다() {
        MigrationVersion higher = MigrationVersion.parse("V31.7.0").orElseThrow();
        MigrationVersion lower = MigrationVersion.parse("V8.0.0").orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(higher.isHigherThan(lower)).isTrue();
            softly.assertThat(lower.isHigherThan(higher)).isFalse();
        });
    }

    @Test
    void compareTo_파트수가_달라도_부족한_자리는_0으로_본다() {
        MigrationVersion shorter = MigrationVersion.parse("V36").orElseThrow();
        MigrationVersion longer = MigrationVersion.parse("V36.0.0").orElseThrow();

        assertThat(shorter.compareTo(longer)).isZero();
    }

    @Test
    void sameMajorWith_메이저가_같은지_판정한다() {
        MigrationVersion first = MigrationVersion.parse("V36.1.0").orElseThrow();
        MigrationVersion second = MigrationVersion.parse("V36.9.9").orElseThrow();
        MigrationVersion other = MigrationVersion.parse("V37.0.0").orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(first.sameMajorWith(second)).isTrue();
            softly.assertThat(first.sameMajorWith(other)).isFalse();
        });
    }
}
