package me.bombom.api.v1.newsletterrequest.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.newsletterrequest.domain.DraftCollectStatus;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;

public record NewsletterRequestSummaryResponse(
        Long id,
        String requestedName,
        String requestedUrl,
        NewsletterRequestStatus status,
        int likeCount,
        DraftCollectStatus collectStatus,
        String draftName,
        String imageUrl,
        LocalDateTime createdAt
) {

    public static NewsletterRequestSummaryResponse of(NewsletterRequest newsletterRequest, NewsletterRequestDraft draft) {
        return new NewsletterRequestSummaryResponse(
                newsletterRequest.getId(),
                newsletterRequest.getRequestedName(),
                newsletterRequest.getRequestedUrl(),
                newsletterRequest.getStatus(),
                newsletterRequest.getLikeCount(),
                draft == null ? null : draft.getCollectStatus(),
                draft == null ? null : draft.getName(),
                draft == null ? null : draft.getImageUrl(),
                newsletterRequest.getCreatedAt()
        );
    }
}
