package me.bombom.api.v1.newsletterrequest.dto.request;

import jakarta.validation.constraints.Size;
import me.bombom.api.v1.newsletterrequest.domain.DraftContent;

/**
 * 초안 수정 요청. 보낸 필드만 바꾼다.
 */
public record UpdateNewsletterRequestDraftRequest(

        @Size(max = 255)
        String name,

        @Size(max = 255)
        String description,

        @Size(max = 512)
        String imageUrl,

        @Size(max = 60)
        String email,

        Long categoryId,

        @Size(max = 512)
        String mainPageUrl,

        @Size(max = 512)
        String subscribeUrl,

        @Size(max = 255)
        String issueCycle,

        @Size(max = 100)
        String sender,

        @Size(max = 512)
        String subscribeMethod,

        @Size(max = 512)
        String previousNewsletterUrl
) {

    public DraftContent toDraftContent() {
        return DraftContent.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .categoryId(categoryId)
                .mainPageUrl(mainPageUrl)
                .subscribeUrl(subscribeUrl)
                .issueCycle(issueCycle)
                .sender(sender)
                .subscribeMethod(subscribeMethod)
                .previousNewsletterUrl(previousNewsletterUrl)
                .build();
    }
}
