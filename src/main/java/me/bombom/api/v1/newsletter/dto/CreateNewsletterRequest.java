package me.bombom.api.v1.newsletter.dto;

import jakarta.validation.constraints.NotBlank;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;

public record CreateNewsletterRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String imageUrl,
        @NotBlank String email,
        @NotBlank String category,

        // Detail fields
        @NotBlank String mainPageUrl,
        @NotBlank String subscribeUrl,
        @NotBlank String issueCycle,
        @NotBlank String sender,
        String previousNewsletterUrl,
        String subscribeMethod,

        // Previous Article Strategy
        NewsletterPreviousStrategy previousStrategy,
        int previousFixedCount,
        int previousRecentCount,
        int previousExposureRatio
) {

    public NewsletterDetail toDetailEntity() {
        return NewsletterDetail.builder()
                .mainPageUrl(mainPageUrl)
                .subscribeUrl(subscribeUrl)
                .issueCycle(issueCycle)
                .sender(sender)
                .previousNewsletterUrl(previousNewsletterUrl)
                .previousAllowed(false)
                .subscribeMethod(subscribeMethod)
                .build();
    }

    public Newsletter toNewsletterEntity(Long detailId, Long categoryId) {
        return Newsletter.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .email(email)
                .categoryId(categoryId)
                .detailId(detailId)
                .build();
    }

    public NewsletterPreviousPolicy toNewsletterPreviousPolicy(
            Long newsletterId
    ) {
        return NewsletterPreviousPolicy.builder()
                .newsletterId(newsletterId)
                .strategy(previousStrategy)
                .fixedCount(previousFixedCount)
                .recentCount(previousRecentCount)
                .exposureRatio(previousExposureRatio)
                .build();
    }
}
