package me.bombom.api.v1.newsletterrequest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

/**
 * 뉴스레터 등록 초안. 수집기가 채우고 운영자가 고친 뒤 승인한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_newsletter_request_draft_request_id", columnNames = {"newsletter_request_id"})
})
public class NewsletterRequestDraft extends BaseEntity {

    private static final int FAILURE_REASON_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long newsletterRequestId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DraftCollectStatus collectStatus;

    @Column(nullable = false)
    private int collectAttemptCount;

    private LocalDateTime collectStartedAt;

    private String failureReason;

    private String name;

    private String description;

    @Column(length = 512)
    private String imageUrl;

    @Column(length = 60)
    private String email;

    private Long categoryId;

    @Column(length = 512)
    private String mainPageUrl;

    @Column(length = 512)
    private String subscribeUrl;

    private String issueCycle;

    @Column(length = 100)
    private String sender;

    @Column(length = 512)
    private String subscribeMethod;

    @Column(length = 512)
    private String previousNewsletterUrl;

    @Builder
    public NewsletterRequestDraft(
            Long id,
            @NonNull Long newsletterRequestId,
            DraftCollectStatus collectStatus,
            int collectAttemptCount,
            LocalDateTime collectStartedAt
    ) {
        this.id = id;
        this.newsletterRequestId = newsletterRequestId;
        this.collectStatus = collectStatus != null ? collectStatus : DraftCollectStatus.PENDING;
        this.collectAttemptCount = collectAttemptCount;
        this.collectStartedAt = collectStartedAt;
    }

    public void applyCollected(@NonNull DraftContent content) {
        this.name = content.name();
        this.description = content.description();
        this.imageUrl = content.imageUrl();
        this.categoryId = content.categoryId();
        this.mainPageUrl = content.mainPageUrl();
        this.subscribeUrl = content.subscribeUrl();
        this.issueCycle = content.issueCycle();
        this.sender = content.sender();
        this.subscribeMethod = content.subscribeMethod();
        this.previousNewsletterUrl = content.previousNewsletterUrl();
        this.collectStatus = DraftCollectStatus.SUCCESS;
        this.failureReason = null;
    }

    /**
     * 수집 실패를 기록한다. 최대 시도 횟수에 닿으면 FAILED로, 아니면 다음 주기에 다시 시도하도록 PENDING으로 둔다.
     */
    public void recordCollectFailure(@NonNull String reason, int maxAttemptCount) {
        this.failureReason = truncate(reason);
        if (collectAttemptCount >= maxAttemptCount) {
            this.collectStatus = DraftCollectStatus.FAILED;
            return;
        }
        this.collectStatus = DraftCollectStatus.PENDING;
    }

    public boolean isCollectFinished() {
        return collectStatus == DraftCollectStatus.SUCCESS || collectStatus == DraftCollectStatus.FAILED;
    }

    public void update(@NonNull DraftContent content, String email) {
        if (content.name() != null) {
            this.name = content.name();
        }
        if (content.description() != null) {
            this.description = content.description();
        }
        if (content.imageUrl() != null) {
            this.imageUrl = content.imageUrl();
        }
        if (content.categoryId() != null) {
            this.categoryId = content.categoryId();
        }
        if (content.mainPageUrl() != null) {
            this.mainPageUrl = content.mainPageUrl();
        }
        if (content.subscribeUrl() != null) {
            this.subscribeUrl = content.subscribeUrl();
        }
        if (content.issueCycle() != null) {
            this.issueCycle = content.issueCycle();
        }
        if (content.sender() != null) {
            this.sender = content.sender();
        }
        if (content.subscribeMethod() != null) {
            this.subscribeMethod = content.subscribeMethod();
        }
        if (content.previousNewsletterUrl() != null) {
            this.previousNewsletterUrl = content.previousNewsletterUrl();
        }
        if (email != null) {
            this.email = email;
        }
    }

    public void resetToPending() {
        this.collectStatus = DraftCollectStatus.PENDING;
        this.collectAttemptCount = 0;
        this.collectStartedAt = null;
        this.failureReason = null;
    }

    /**
     * 뉴스레터 생성에 꼭 필요한데 비어 있는 필드 이름 목록.
     */
    public List<String> findMissingRequiredFields() {
        List<String> missingFields = new ArrayList<>();
        addIfBlank(missingFields, "name", name);
        addIfBlank(missingFields, "description", description);
        addIfBlank(missingFields, "imageUrl", imageUrl);
        addIfBlank(missingFields, "email", email);
        addIfBlank(missingFields, "mainPageUrl", mainPageUrl);
        addIfBlank(missingFields, "subscribeUrl", subscribeUrl);
        addIfBlank(missingFields, "issueCycle", issueCycle);
        addIfBlank(missingFields, "sender", sender);
        if (categoryId == null) {
            missingFields.add("categoryId");
        }
        return missingFields;
    }

    private static void addIfBlank(
            List<String> missingFields,
            String fieldName,
            String value
    ) {
        if (value == null || value.isBlank()) {
            missingFields.add(fieldName);
        }
    }

    private static String truncate(String reason) {
        if (reason.length() <= FAILURE_REASON_MAX_LENGTH) {
            return reason;
        }
        return reason.substring(0, FAILURE_REASON_MAX_LENGTH);
    }
}
