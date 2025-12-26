package me.bombom.api.v1.newsletter.dto;

import com.querydsl.core.annotations.QueryProjection;

public record GetNewsletterSummaryResponse(
        Long id,
        String name,
        String imageUrl,
        String categoryName,
        String issueCycle,
        int subscriptionCount,
        String previousStrategy) {

    @QueryProjection
    public GetNewsletterSummaryResponse {
    }
}
