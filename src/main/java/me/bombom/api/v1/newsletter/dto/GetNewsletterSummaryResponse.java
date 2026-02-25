package me.bombom.api.v1.newsletter.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;

public record GetNewsletterSummaryResponse(
        Long id,
        String name,
        String imageUrl,
        String categoryName,
        String issueCycle,
        int subscriptionCount,
        String previousStrategy,
        String status,
        LocalDate suspendedAt
) {

    @QueryProjection
    public GetNewsletterSummaryResponse {
    }
}
