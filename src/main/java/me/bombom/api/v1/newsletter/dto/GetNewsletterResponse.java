package me.bombom.api.v1.newsletter.dto;

import com.querydsl.core.annotations.QueryProjection;

public record GetNewsletterResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        String email,
        String categoryName,

        // Detail fields
        String mainPageUrl,
        String subscribeUrl,
        String issueCycle,
        int subscriptionCount,
        String sender,
        String previousNewsletterUrl,
        boolean previousAllowed,
        String subscribeMethod,

        // Previous Policy fields
        String previousStrategy,
        int previousFixedCount,
        int previousRecentCount,
        int previousExposureRatio) {

    @QueryProjection
    public GetNewsletterResponse {
    }
}
