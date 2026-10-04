package me.bombom.api.v1.newsletterrequest.dto.response;

import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;

public record CollectTarget(
        Long draftId,
        Long newsletterRequestId,
        String requestedName,
        String requestedUrl
) {

    public static CollectTarget of(NewsletterRequestDraft draft, NewsletterRequest newsletterRequest) {
        return new CollectTarget(
                draft.getId(),
                newsletterRequest.getId(),
                newsletterRequest.getRequestedName(),
                newsletterRequest.getRequestedUrl()
        );
    }
}
