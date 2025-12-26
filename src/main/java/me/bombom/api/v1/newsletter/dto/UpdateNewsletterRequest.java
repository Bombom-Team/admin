package me.bombom.api.v1.newsletter.dto;

import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;

public record UpdateNewsletterRequest(

        String name,
        String description,
        String imageUrl,
        String email,
        String category,

        String mainPageUrl,
        String subscribeUrl,
        String issueCycle,
        String sender,
        String previousNewsletterUrl,
        Boolean previousAllowed,
        String subscribeMethod,

        NewsletterPreviousStrategy previousStrategy,
        Integer previousFixedCount,
        Integer previousRecentCount,
        Integer previousExposureRatio
) {
}
