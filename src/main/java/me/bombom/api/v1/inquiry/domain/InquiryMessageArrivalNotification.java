package me.bombom.api.v1.inquiry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryMessageArrivalNotification extends BaseEntity {

    private static final String STATUS_PENDING = "PENDING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int attempts;

    private LocalDateTime nextRetryAt;

    private String lastError;

    @Column(nullable = false)
    private Long messageId;

    public InquiryMessageArrivalNotification(Long memberId, Long messageId) {
        this.memberId = memberId;
        this.messageId = messageId;
        this.status = STATUS_PENDING;
        this.attempts = 0;
    }
}
