package me.bombom.api.v1.inquiry.dto.request;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;

public record UpdateInquiryRoomStatusRequest(

        @NotNull
        InquiryStatus status
) {
}
