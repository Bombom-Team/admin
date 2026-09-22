package me.bombom.api.v1.inquiry.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignInquiryRoomRequest(

        @NotNull
        Long assigneeId
) {
}
