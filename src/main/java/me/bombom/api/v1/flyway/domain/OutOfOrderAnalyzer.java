package me.bombom.api.v1.flyway.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 추적 중인 마이그레이션 목록에서 out-of-order 위험을 계산한다.
 * - 순서 역전: 낮은 번호(미적용)가 더 높은 번호(먼저 적용/머지) 뒤에 끼어들며 같은 테이블/컬럼을 건드림
 * - 번호 충돌: 같은 버전 번호를 둘 이상이 사용
 */
public class OutOfOrderAnalyzer {

    public List<LeapfrogWarning> detectLeapfrogs(List<TrackedMigration> migrations) {
        List<LeapfrogWarning> warnings = new ArrayList<>();
        for (TrackedMigration mine : migrations) {
            if (mine.isPending()) {
                warnings.addAll(leapfrogsFor(mine, migrations));
            }
        }

        return warnings;
    }

    public List<VersionConflict> detectVersionConflicts(List<TrackedMigration> migrations) {
        List<VersionConflict> conflicts = new ArrayList<>();
        for (List<TrackedMigration> sameVersion : groupByVersion(migrations).values()) {
            if (sameVersion.size() > 1) {
                conflicts.add(new VersionConflict(sameVersion.get(0).version(), sameVersion));
            }
        }

        return conflicts;
    }

    private List<LeapfrogWarning> leapfrogsFor(TrackedMigration mine, List<TrackedMigration> migrations) {
        List<LeapfrogWarning> warnings = new ArrayList<>();
        for (TrackedMigration ahead : migrations) {
            ConflictSeverity severity = mine.severityAgainst(ahead);
            if (isLeapfrog(mine, ahead) && severity.isWarning()) {
                warnings.add(new LeapfrogWarning(mine, ahead, severity));
            }
        }

        return warnings;
    }

    private boolean isLeapfrog(TrackedMigration mine, TrackedMigration ahead) {
        return ahead.version().isHigherThan(mine.version())
                && ahead.status().isAheadOf(mine.status());
    }

    private Map<String, List<TrackedMigration>> groupByVersion(List<TrackedMigration> migrations) {
        Map<String, List<TrackedMigration>> grouped = new LinkedHashMap<>();
        for (TrackedMigration migration : migrations) {
            grouped.computeIfAbsent(migration.version().raw(), key -> new ArrayList<>())
                    .add(migration);
        }

        return grouped;
    }
}
