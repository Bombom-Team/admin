package me.bombom.api.v1.flyway.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flyway 버전 식별자. {@code V31.7.0} 처럼 점으로 구분된 숫자 버전을 표현하며,
 * out-of-order 충돌 판정을 위해 시맨틱(숫자) 정렬을 제공한다.
 */
public record MigrationVersion(
        List<Integer> parts,
        String raw
) implements Comparable<MigrationVersion> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("V(\\d+(?:\\.\\d+)*)");

    public static Optional<MigrationVersion> parse(String versionText) {
        Matcher matcher = VERSION_PATTERN.matcher(versionText);
        if (matcher.matches()) {
            return Optional.of(new MigrationVersion(toParts(matcher.group(1)), versionText));
        }

        return Optional.empty();
    }

    public boolean isHigherThan(MigrationVersion other) {
        return compareTo(other) > 0;
    }

    public boolean sameMajorWith(MigrationVersion other) {
        return major() == other.major();
    }

    public int major() {
        return parts.isEmpty() ? 0 : parts.get(0);
    }

    public int minor() {
        return parts.size() > 1 ? parts.get(1) : 0;
    }

    @Override
    public int compareTo(MigrationVersion other) {
        int size = Math.max(parts.size(), other.parts.size());
        for (int index = 0; index < size; index += 1) {
            int left = partAt(index);
            int right = other.partAt(index);
            if (left != right) {
                return Integer.compare(left, right);
            }
        }

        return 0;
    }

    private int partAt(int index) {
        return index < parts.size() ? parts.get(index) : 0;
    }

    private static List<Integer> toParts(String numericVersion) {
        List<Integer> parts = new ArrayList<>();
        for (String token : numericVersion.split("\\.")) {
            parts.add(Integer.parseInt(token));
        }

        return parts;
    }
}
