package me.bombom.api.v1.flyway.github;

public record GitHubPullFile(
        String filename,
        String status,
        String patch
) {
}
