package me.bombom.api.v1.newsletterrequest.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;

public record NewsletterRequestDetailResponse(
        Long id,
        String requestedName,
        String requestedUrl,
        Long requesterMemberId,
        NewsletterRequestStatus status,
        int supporterCount,
        Long newsletterId,
        String rejectReason,
        LocalDateTime createdAt,
        NewsletterRequestDraftResponse draft,
        List<NewsletterRequestReasonResponse> reasons
) {

    public static NewsletterRequestDetailResponse of(
            NewsletterRequest newsletterRequest,
            NewsletterRequestDraftResponse draft,
            List<NewsletterRequestReasonResponse> reasons
    ) {
        return new NewsletterRequestDetailResponse(
                newsletterRequest.getId(),
                newsletterRequest.getRequestedName(),
                newsletterRequest.getRequestedUrl(),
                newsletterRequest.getRequesterMemberId(),
                newsletterRequest.getStatus(),
                newsletterRequest.getSupporterCount(),
                newsletterRequest.getNewsletterId(),
                newsletterRequest.getRejectReason(),
                newsletterRequest.getCreatedAt(),
                draft,
                reasons
        );
    }
}
