package me.bombom.api.v1.inquiry.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;

public record LastMessageResponse(
        String content,
        boolean hasImages,
        InquirySenderType senderType,
        String adminNickname,
        LocalDateTime createdAt
) {

    public static LastMessageResponse of(InquiryMessage message, boolean hasImages, String adminNickname) {
        return new LastMessageResponse(
                message.getContent(),
                hasImages,
                message.getSenderType(),
                message.getSenderType() == InquirySenderType.ADMIN ? adminNickname : null,
                message.getCreatedAt());
    }
}
