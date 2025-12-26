package me.bombom.api.v1.newsletter.dto;

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
        String subscribeMethod
) {
}
