package me.bombom.api.v1.dev.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.dev.dto.LambdaPlaywrightSourceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class LambdaPlaywrightService {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String FILE_PATH = "src/main/resources/lambda-playwright/index.js";

    private final WebClient.Builder webClientBuilder;

    @Value("${github.owner}")
    private String owner;

    @Value("${github.repo}")
    private String repo;

    @Value("${github.branch:server}")
    private String branch;

    @Value("${github.token}")
    private String token;

    public String getSource() {
        try {
            WebClient client = createClient();

            GithubContentResponse response = client.get()
                    .uri("/repos/{owner}/{repo}/contents/{path}?ref={branch}", owner, repo, FILE_PATH, branch)
                    .retrieve()
                    .bodyToMono(GithubContentResponse.class)
                    .block();

            if (response == null || response.content() == null) {
                throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                        .addContext(ErrorContextKeys.OPERATION, "githubGetContent");
            }

            byte[] decoded = Base64.getDecoder()
                    .decode(response.content().replace("\n", ""));
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (WebClientResponseException e) {
            log.error("GitHub API error when getting content", e);
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "githubGetContent")
                    .addContext("status", e.getStatusCode().value());
        } catch (Exception e) {
            log.error("Unexpected error when getting lambda playwright source", e);
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "githubGetContent");
        }
    }

    public void updateSource(LambdaPlaywrightSourceRequest request) {
        try {
            WebClient client = createClient();

            GithubContentResponse current = client.get()
                    .uri("/repos/{owner}/{repo}/contents/{path}?ref={branch}",
                            owner, repo, FILE_PATH, branch)
                    .retrieve()
                    .bodyToMono(GithubContentResponse.class)
                    .block();

            if (current == null || current.sha() == null) {
                throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                        .addContext(ErrorContextKeys.OPERATION, "githubGetContentSha");
            }

            String encodedContent = Base64.getEncoder()
                    .encodeToString(request.content().getBytes(StandardCharsets.UTF_8));

            GithubUpdateFileRequest updateRequest = new GithubUpdateFileRequest(
                    "chore: AWS lambda-playwright 스크립트 수정",
                    encodedContent,
                    current.sha(),
                    branch
            );

            client.put()
                    .uri("/repos/{owner}/{repo}/contents/{path}",
                            owner, repo, FILE_PATH)
                    .bodyValue(updateRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .onErrorResume(throwable -> {
                        if (throwable instanceof WebClientResponseException exception) {
                            log.error("GitHub API error when updating content. status: {}, body: {}",
                                    exception.getStatusCode().value(), exception.getResponseBodyAsString());
                        } else {
                            log.error("Unexpected error when updating GitHub content", throwable);
                        }
                        return Mono.error(throwable);
                    })
                    .block();
        } catch (WebClientResponseException e) {
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "githubUpdateContent")
                    .addContext("status", e.getStatusCode().value());
        } catch (Exception e) {
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "githubUpdateContent");
        }
    }

    private WebClient createClient() {
        return webClientBuilder
                .baseUrl(GITHUB_API_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .build();
    }

    private record GithubContentResponse(

            String content,
            String sha
    ) {
    }

    private record GithubUpdateFileRequest(

            String message,
            String content,
            String sha,
            String branch
    ) {
    }
}
