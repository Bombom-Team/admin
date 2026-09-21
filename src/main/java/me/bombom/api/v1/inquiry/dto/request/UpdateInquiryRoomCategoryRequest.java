package me.bombom.api.v1.inquiry.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateInquiryRoomCategoryRequest(

        @NotNull
        Long categoryId
) {
}
