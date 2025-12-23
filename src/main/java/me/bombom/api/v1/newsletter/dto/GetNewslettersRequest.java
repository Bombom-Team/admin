package me.bombom.api.v1.newsletter.dto;

public record GetNewslettersRequest(

        String keyword,
        String category,
        NewsletterSortType sort
) {
}
