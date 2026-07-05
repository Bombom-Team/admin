package me.bombom.api.v1.flyway.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GitHubIssue(
        int number,
        String title,
        String body,
        String htmlUrl,
        GitHubUser user,
        Object pullRequest
) {

    public boolean isPullRequest() {
        return pullRequest != null;
    }

    public String authorLogin() {
        return user == null ? "" : user.login();
    }
}
