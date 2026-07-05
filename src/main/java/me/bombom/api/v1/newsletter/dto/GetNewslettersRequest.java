package me.bombom.api.v1.newsletter.dto;

import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;

public record GetNewslettersRequest(

        String keyword,
        String category,
        NewsletterPreviousStrategy previousStrategy,
        NewsletterStatusFilter status,
        NewsletterSortType sort
) {
}
