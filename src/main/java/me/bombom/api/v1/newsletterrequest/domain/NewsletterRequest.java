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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;

/**
 * 유저의 뉴스레터 신청. 테이블은 운영 서버 Flyway(V39.0.0)가 관리한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_newsletter_request_normalized_url", columnNames = {"normalized_url"})
})
public class NewsletterRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String requestedName;

    @Column(nullable = false, length = 512)
    private String requestedUrl;

    @Column(nullable = false, length = 512)
    private String normalizedUrl;

    @Column(nullable = false)
    private Long requesterMemberId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NewsletterRequestStatus status;

    @Column(nullable = false)
    private int supporterCount;

    private Long newsletterId;

    private String rejectReason;

    @Builder
    public NewsletterRequest(
            Long id,
            @NonNull String requestedName,
            @NonNull String requestedUrl,
            @NonNull String normalizedUrl,
            @NonNull Long requesterMemberId,
            NewsletterRequestStatus status,
            int supporterCount
    ) {
        this.id = id;
        this.requestedName = requestedName;
        this.requestedUrl = requestedUrl;
        this.normalizedUrl = normalizedUrl;
        this.requesterMemberId = requesterMemberId;
        this.status = status != null ? status : NewsletterRequestStatus.RECEIVED;
        this.supporterCount = supporterCount > 0 ? supporterCount : 1;
    }

    public void markReviewing() {
        if (status == NewsletterRequestStatus.RECEIVED) {
            this.status = NewsletterRequestStatus.REVIEWING;
        }
    }

    public void approve(@NonNull Long newsletterId) {
        validateInProgress("approve");
        this.status = NewsletterRequestStatus.APPROVED;
        this.newsletterId = newsletterId;
    }

    public void reject(@NonNull String rejectReason) {
        validateInProgress("reject");
        this.status = NewsletterRequestStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

    private void validateInProgress(String operation) {
        if (status.isClosed()) {
            throw new CIllegalArgumentException(ErrorDetail.RESOURCE_CONFLICT)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequest")
                    .addContext(ErrorContextKeys.OPERATION, operation)
                    .addContext("newsletterRequestId", id)
                    .addContext("status", status);
        }
    }
}
