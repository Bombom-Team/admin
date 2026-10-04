package me.bombom.api.v1.newsletterrequest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * 신청에 공감한 회원(최초 신청자 포함). 어드민은 추천 이유를 읽기만 한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_newsletter_request_supporter_request_member",
                columnNames = {"newsletter_request_id", "member_id"}
        )
})
public class NewsletterRequestSupporter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long newsletterRequestId;

    @Column(nullable = false)
    private Long memberId;

    @Column(length = 200)
    private String reason;

    @Column(nullable = false)
    private boolean notifyEnabled;

    @Builder
    public NewsletterRequestSupporter(
            Long id,
            @NonNull Long newsletterRequestId,
            @NonNull Long memberId,
            String reason,
            boolean notifyEnabled
    ) {
        this.id = id;
        this.newsletterRequestId = newsletterRequestId;
        this.memberId = memberId;
        this.reason = reason;
        this.notifyEnabled = notifyEnabled;
    }
}
