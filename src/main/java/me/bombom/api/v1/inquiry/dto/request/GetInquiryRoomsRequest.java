package me.bombom.api.v1.inquiry.dto.request;

import me.bombom.api.v1.inquiry.domain.InquiryStatus;

public record GetInquiryRoomsRequest(

        InquiryStatus status,
        Long assigneeId,
        Long categoryId
) {
}
