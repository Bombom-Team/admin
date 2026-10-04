package me.bombom.api.v1.newsletterrequest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectNewsletterRequestRequest(

        @NotBlank
        @Size(max = 255)
        String reason
) {
}
