package me.bombom.api.v1.flyway.github;

public record GitHubContentItem(
        String name,
        String path,
        String type,
        String sha
) {

    public boolean isFile() {
        return "file".equals(type);
    }
}
