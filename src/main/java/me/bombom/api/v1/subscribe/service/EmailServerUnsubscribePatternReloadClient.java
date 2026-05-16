package me.bombom.api.v1.subscribe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServerUnsubscribePatternReloadClient {

    private static final String RELOAD_PATH = "/internal/v1/unsubscribe-patterns/reload";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final WebClient.Builder webClientBuilder;

    @Value("${email-server.base-url}")
    private String emailServerBaseUrl;

    @Value("${MAIL_SERVER_INTERNAL_API_KEY:}")
    private String internalApiKey;

    public void reload() {
        try {
            createClient()
                    .post()
                    .uri(RELOAD_PATH)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Email server unsubscribe pattern reload API error", e);
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "emailServerUnsubscribePatternReload")
                    .addContext("status", e.getStatusCode().value());
        } catch (Exception e) {
            log.error("Unexpected error when reloading email server unsubscribe patterns", e);
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "emailServerUnsubscribePatternReload");
        }
    }

    private WebClient createClient() {
        return webClientBuilder
                .baseUrl(emailServerBaseUrl)
                .defaultHeader(INTERNAL_API_KEY_HEADER, internalApiKey)
                .build();
    }
}
