package me.bombom.api.v1.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAdminInquiryMessageRequest(

        @NotBlank
        @Size(max = 500)
        String content
) {
}
