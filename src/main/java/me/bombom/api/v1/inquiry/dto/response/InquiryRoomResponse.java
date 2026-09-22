package me.bombom.api.v1.inquiry.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.inquiry.domain.InquirerType;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;

public record InquiryRoomResponse(

        Long id,
        Long categoryId,
        InquiryStatus status,
        Long assigneeId,
        String assigneeNickname,
        InquirerType inquirerType,
        String guestId,
        String inquirerNickname,
        String inquirerEmail,
        LastMessageResponse lastMessage,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {

    public static InquiryRoomResponse of(
            InquiryRoom room,
            String assigneeNickname,
            InquirerType inquirerType,
            String inquirerNickname,
            String inquirerEmail,
            LastMessageResponse lastMessage) {
        return new InquiryRoomResponse(
                room.getId(),
                room.getCategoryId(),
                room.getStatus(),
                room.getAssigneeId(),
                assigneeNickname,
                inquirerType,
                room.getGuestId(),
                inquirerNickname,
                inquirerEmail,
                lastMessage,
                room.getCreatedAt(),
                room.getClosedAt());
    }
}
