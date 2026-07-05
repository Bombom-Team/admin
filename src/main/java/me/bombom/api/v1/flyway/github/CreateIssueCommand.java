package me.bombom.api.v1.flyway.github;

import java.util.List;

public record CreateIssueCommand(
        String title,
        String body,
        List<String> labels
) {
}
