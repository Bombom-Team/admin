package me.bombom.api.v1.subscribe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnsubscribeRetry extends BaseEntity {

    private static final List<Duration> BACKOFF_STRATEGY = List.of(
            Duration.ofMinutes(10), // 네트워크 문제
            Duration.ofMinutes(30), // 짧은 점검
            Duration.ofHours(2) // 점검
    );
    private static final int MAX_RETRY_COUNT = BACKOFF_STRATEGY.size();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long subscribeId;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private LocalDateTime nextRetryAt;

    private String lastError;

    @Builder
    public UnsubscribeRetry(
            Long id,
            @NonNull Long subscribeId,
            @NonNull LocalDateTime nextRetryAt,
            String lastError) {
        this.id = id;
        this.subscribeId = subscribeId;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.retryCount = 0;
    }
}
