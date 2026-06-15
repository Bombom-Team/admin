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
 * GitHub REST API 호출 래퍼.
 * - 소스 레포(public) 읽기: 토큰 미첨부(public read). admin용 토큰은 다른 org라 붙이면 오히려 거부될 수 있음.
 * - 이슈 레포(admin) 조회/생성: 토큰 첨부.
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
                false,
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
                false,
                owner, repo, path, ref);
    }

    public List<GitHubPullRequest> listOpenPullRequests(String owner, String repo) {
        GitHubPullRequest[] pulls = getForObject(
                "/repos/{owner}/{repo}/pulls?state=open&per_page=100",
                GitHubPullRequest[].class,
                false,
                owner, repo);
        return toList(pulls);
    }

    public List<GitHubPullFile> listPullFiles(String owner, String repo, int number) {
        GitHubPullFile[] files = getForObject(
                "/repos/{owner}/{repo}/pulls/{number}/files?per_page=100",
                GitHubPullFile[].class,
                false,
                owner, repo, number);
        return toList(files);
    }

    public List<GitHubIssue> listOpenIssuesWithLabel(String owner, String repo, String label) {
        GitHubIssue[] issues = getForObject(
                "/repos/{owner}/{repo}/issues?state=open&labels={label}&per_page=100",
                GitHubIssue[].class,
                true,
                owner, repo, label);
        return toList(issues);
    }

    public GitHubIssue createIssue(String owner, String repo, CreateIssueCommand command) {
        try {
            return client(true).post()
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

    private <T> T getForObject(String uri, Class<T> type, boolean authenticated, Object... uriVariables) {
        try {
            return client(authenticated).get()
                    .uri(uri, uriVariables)
                    .retrieve()
                    .bodyToMono(type)
                    .block();
        } catch (WebClientResponseException.NotFound notFound) {
            return null;
        } catch (WebClientResponseException.Unauthorized unauthorized) {
            // 토큰이 만료/취소된 경우 — 인증 필요 기능(이슈 조회)은 빈 결과로 처리
            log.warn("GitHub 인증 실패(401), 빈 결과 반환: {}", uri);
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

    private WebClient client(boolean authenticated) {
        WebClient.Builder builder = webClientBuilder.clone()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");
        if (authenticated && properties.hasIssueToken()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getIssueToken());
        }

        return builder.build();
    }
}
