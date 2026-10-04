package me.bombom.api.v1.newsletterrequest.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestSupporter;

public record NewsletterRequestReasonResponse(
        Long memberId,
        String reason,
        LocalDateTime createdAt
) {

    public static NewsletterRequestReasonResponse from(NewsletterRequestSupporter supporter) {
        return new NewsletterRequestReasonResponse(
                supporter.getMemberId(),
                supporter.getReason(),
                supporter.getCreatedAt()
        );
    }
}
