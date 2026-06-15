package me.bombom.api.v1.flyway.github;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.flyway.config.FlywayMonitorProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * GitHub REST API 호출 래퍼. 읽기는 public repo라 토큰 없이도 동작하고,
 * 이슈 생성은 GITHUB_ISSUE_TOKEN 이 있을 때만 가능하다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubClient {

    private static final String BASE_URL = "https://api.github.com";

    private final WebClient.Builder webClientBuilder;
    private final FlywayMonitorProperties properties;

    public List<GitHubContentItem> listDirectory(
            String owner,
            String repo,
            String path,
            String ref
    ) {
        GitHubContentItem[] items = getForObject(
                "/repos/{owner}/{repo}/contents/{path}?ref={ref}",
                GitHubContentItem[].class,
                owner, repo, path, ref);
        return toList(items);
    }

    public GitHubFileContent getFileContent(
            String owner,
            String repo,
            String path,
            String ref
    ) {
        return getForObject(
                "/repos/{owner}/{repo}/contents/{path}?ref={ref}",
                GitHubFileContent.class,
                owner, repo, path, ref);
    }

    public List<GitHubPullRequest> listOpenPullRequests(String owner, String repo) {
        GitHubPullRequest[] pulls = getForObject(
                "/repos/{owner}/{repo}/pulls?state=open&per_page=100",
                GitHubPullRequest[].class,
                owner, repo);
        return toList(pulls);
    }

    public List<GitHubPullFile> listPullFiles(String owner, String repo, int number) {
        GitHubPullFile[] files = getForObject(
                "/repos/{owner}/{repo}/pulls/{number}/files?per_page=100",
                GitHubPullFile[].class,
                owner, repo, number);
        return toList(files);
    }

    public List<GitHubIssue> listOpenIssuesWithLabel(String owner, String repo, String label) {
        GitHubIssue[] issues = getForObject(
                "/repos/{owner}/{repo}/issues?state=open&labels={label}&per_page=100",
                GitHubIssue[].class,
                owner, repo, label);
        return toList(issues);
    }

    public GitHubIssue createIssue(String owner, String repo, CreateIssueCommand command) {
        try {
            return createClient().post()
                    .uri("/repos/{owner}/{repo}/issues", owner, repo)
                    .bodyValue(command)
                    .retrieve()
                    .bodyToMono(GitHubIssue.class)
                    .block();
        } catch (WebClientResponseException exception) {
            throw externalError("githubCreateIssue", exception.getStatusCode().value());
        } catch (Exception exception) {
            log.error("GitHub 이슈 생성 실패", exception);
            throw externalError("githubCreateIssue", -1);
        }
    }

    private <T> T getForObject(String uri, Class<T> type, Object... uriVariables) {
        try {
            return createClient().get()
                    .uri(uri, uriVariables)
                    .retrieve()
                    .bodyToMono(type)
                    .block();
        } catch (WebClientResponseException.NotFound notFound) {
            return null;
        } catch (WebClientResponseException exception) {
            throw externalError("githubGet", exception.getStatusCode().value());
        } catch (Exception exception) {
            log.error("GitHub API 호출 실패: {}", uri, exception);
            throw externalError("githubGet", -1);
        }
    }

    private CServerErrorException externalError(String operation, int status) {
        return new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                .addContext(ErrorContextKeys.OPERATION, operation)
                .addContext("status", status);
    }

    private <T> List<T> toList(T[] values) {
        if (values == null) {
            return List.of();
        }

        return Arrays.stream(values)
                .toList();
    }

    private WebClient createClient() {
        WebClient.Builder builder = webClientBuilder.clone()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");
        if (properties.hasIssueToken()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getIssueToken());
        }

        return builder.build();
    }
}
