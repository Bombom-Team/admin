package me.bombom.api.v1.newsletterrequest.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 뉴스레터 신청 자동 수집 설정. application.yml 없이도 동작하도록 기본값을 코드에 둔다.
 * 수집기는 newsletter-request.collector.enabled=true이고 API 키가 있을 때만 켜진다.
 */
@Getter
@Component
public class NewsletterRequestCollectProperties {

    @Value("${newsletter-request.collector.enabled:false}")
    private boolean enabled;

    @Value("${newsletter-request.collector.anthropic-api-key:}")
    private String anthropicApiKey;

    @Value("${newsletter-request.collector.model:claude-opus-5-5}")
    private String model;

    @Value("${newsletter-request.collector.batch-size:5}")
    private int batchSize;

    @Value("${newsletter-request.collector.max-attempt-count:3}")
    private int maxAttemptCount;

    @Value("${newsletter-request.collector.stale-minutes:10}")
    private long staleMinutes;

    @Value("${newsletter-request.collector.fetch-timeout-millis:10000}")
    private int fetchTimeoutMillis;

    @Value("${newsletter-request.collector.thumbnail-prefix:newsletters}")
    private String thumbnailPrefix;
}
