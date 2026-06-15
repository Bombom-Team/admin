package me.bombom.api.v1.flyway.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GitHubPullRequest(
        int number,
        String title,
        String htmlUrl,
        GitHubUser user,
        GitHubRef head
) {

    public String authorLogin() {
        return user == null ? "" : user.login();
    }

    public String branch() {
        return head == null ? "" : head.ref();
    }
}
