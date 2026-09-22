package me.bombom.api.v1.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SendAdminInquiryMessageRequest(

        @NotBlank
        @Size(max = 500)
        String content,

        @Size(max = 4)
        List<String> imageUrls
) {
}
