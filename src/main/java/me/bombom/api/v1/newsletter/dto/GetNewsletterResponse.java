package me.bombom.api.v1.newsletter.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;

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
        int previousExposureRatio,

        // Publication Status fields
        String status,
        LocalDate suspendedAt
) {

    @QueryProjection
    public GetNewsletterResponse {
    }
}
