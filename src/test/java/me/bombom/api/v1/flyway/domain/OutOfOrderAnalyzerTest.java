package me.bombom.api.v1.flyway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import org.junit.jupiter.api.Test;

class OutOfOrderAnalyzerTest {

    private final OutOfOrderAnalyzer analyzer = new OutOfOrderAnalyzer();

    @Test
    void detectLeapfrogs_낮은번호가_먼저적용된_높은번호와_같은컬럼이면_강한경고() {
        TrackedMigration mine = tracked("V36.1.0", MigrationStatus.PR_REVIEW,
                "ALTER TABLE member ADD COLUMN grade VARCHAR(10);");
        TrackedMigration ahead = tracked("V36.2.0", MigrationStatus.DB_APPLIED,
                "ALTER TABLE member MODIFY COLUMN grade INT;");

        List<LeapfrogWarning> warnings = analyzer.detectLeapfrogs(List.of(mine, ahead));

        assertThat(warnings).hasSize(1);
        assertSoftly(softly -> {
            softly.assertThat(warnings.get(0).mine()).isEqualTo(mine);
            softly.assertThat(warnings.get(0).ahead()).isEqualTo(ahead);
            softly.assertThat(warnings.get(0).severity()).isEqualTo(ConflictSeverity.COLUMN);
        });
    }

    @Test
    void detectLeapfrogs_같은테이블_다른컬럼이면_약한경고() {
        TrackedMigration mine = tracked("V36.1.0", MigrationStatus.PR_REVIEW,
                "ALTER TABLE member ADD COLUMN grade VARCHAR(10);");
        TrackedMigration ahead = tracked("V36.2.0", MigrationStatus.DB_APPLIED,
                "ALTER TABLE member ADD COLUMN nickname VARCHAR(30);");

        List<LeapfrogWarning> warnings = analyzer.detectLeapfrogs(List.of(mine, ahead));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.get(0).severity()).isEqualTo(ConflictSeverity.TABLE);
    }

    @Test
    void detectLeapfrogs_다른테이블이면_경고없음() {
        TrackedMigration mine = tracked("V36.1.0", MigrationStatus.LOCAL_WIP,
                "ALTER TABLE member_contact ADD COLUMN phone VARCHAR(20);");
        TrackedMigration ahead = tracked("V36.2.0", MigrationStatus.DB_APPLIED,
                "ALTER TABLE member ADD COLUMN grade VARCHAR(10);");

        assertThat(analyzer.detectLeapfrogs(List.of(mine, ahead))).isEmpty();
    }

    @Test
    void detectLeapfrogs_더_높은게_아직_안앞서있으면_경고없음() {
        TrackedMigration mine = tracked("V36.1.0", MigrationStatus.PR_REVIEW,
                "ALTER TABLE member ADD COLUMN grade VARCHAR(10);");
        TrackedMigration sameStage = tracked("V36.2.0", MigrationStatus.PR_REVIEW,
                "ALTER TABLE member MODIFY COLUMN grade INT;");

        assertThat(analyzer.detectLeapfrogs(List.of(mine, sameStage))).isEmpty();
    }

    @Test
    void detectVersionConflicts_같은_버전번호가_둘이면_충돌로_묶는다() {
        TrackedMigration first = tracked("V36.1.0", MigrationStatus.PR_REVIEW,
                "ALTER TABLE newsletter ADD COLUMN open_rate DECIMAL(5,2);");
        TrackedMigration second = tracked("V36.1.0", MigrationStatus.PR_REVIEW,
                "CREATE INDEX idx_nd_member ON newsletter_detail (member_id);");
        TrackedMigration unique = tracked("V37.0.0", MigrationStatus.LOCAL_WIP,
                "CREATE TABLE reading_streak (id BIGINT);");

        List<VersionConflict> conflicts = analyzer.detectVersionConflicts(List.of(first, second, unique));

        assertThat(conflicts).hasSize(1);
        assertSoftly(softly -> {
            softly.assertThat(conflicts.get(0).version().raw()).isEqualTo("V36.1.0");
            softly.assertThat(conflicts.get(0).migrations()).containsExactly(first, second);
        });
    }

    private TrackedMigration tracked(String version, MigrationStatus status, String sql) {
        return new TrackedMigration(
                MigrationVersion.parse(version).orElseThrow(),
                "test",
                status,
                MigrationScript.analyze(sql));
    }
}
