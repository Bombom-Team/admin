package me.bombom.api.v1.newsletterrequest.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.newsletterrequest.domain.DraftCollectStatus;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;

public record NewsletterRequestDraftResponse(
        DraftCollectStatus collectStatus,
        int collectAttemptCount,
        LocalDateTime collectStartedAt,
        String failureReason,
        String name,
        String description,
        String imageUrl,
        String email,
        Long categoryId,
        String mainPageUrl,
        String subscribeUrl,
        String issueCycle,
        String sender,
        String subscribeMethod,
        String previousNewsletterUrl,
        List<String> missingFields
) {

    public static NewsletterRequestDraftResponse from(NewsletterRequestDraft draft) {
        return new NewsletterRequestDraftResponse(
                draft.getCollectStatus(),
                draft.getCollectAttemptCount(),
                draft.getCollectStartedAt(),
                draft.getFailureReason(),
                draft.getName(),
                draft.getDescription(),
                draft.getImageUrl(),
                draft.getEmail(),
                draft.getCategoryId(),
                draft.getMainPageUrl(),
                draft.getSubscribeUrl(),
                draft.getIssueCycle(),
                draft.getSender(),
                draft.getSubscribeMethod(),
                draft.getPreviousNewsletterUrl(),
                draft.findMissingRequiredFields()
        );
    }
}
