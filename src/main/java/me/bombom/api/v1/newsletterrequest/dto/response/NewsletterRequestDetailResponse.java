package me.bombom.api.v1.newsletterrequest.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;

public record NewsletterRequestDetailResponse(
        Long id,
        String requestedName,
        String requestedUrl,
        Long requesterMemberId,
        NewsletterRequestStatus status,
        String reason,
        int likeCount,
        Long newsletterId,
        String rejectReason,
        LocalDateTime createdAt,
        NewsletterRequestDraftResponse draft
) {

    public static NewsletterRequestDetailResponse of(
            NewsletterRequest newsletterRequest,
            NewsletterRequestDraftResponse draft
    ) {
        return new NewsletterRequestDetailResponse(
                newsletterRequest.getId(),
                newsletterRequest.getRequestedName(),
                newsletterRequest.getRequestedUrl(),
                newsletterRequest.getRequesterMemberId(),
                newsletterRequest.getStatus(),
                newsletterRequest.getReason(),
                newsletterRequest.getLikeCount(),
                newsletterRequest.getNewsletterId(),
                newsletterRequest.getRejectReason(),
                newsletterRequest.getCreatedAt(),
                draft
        );
    }
}
