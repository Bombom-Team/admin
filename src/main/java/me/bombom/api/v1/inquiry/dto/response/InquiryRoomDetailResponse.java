package me.bombom.api.v1.inquiry.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;

public record InquiryRoomDetailResponse(

        Long id,
        Long memberId,
        String guestId,
        Long categoryId,
        InquiryStatus status,
        Long assigneeId,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {

    public static InquiryRoomDetailResponse from(InquiryRoom room) {
        return new InquiryRoomDetailResponse(
                room.getId(),
                room.getMemberId(),
                room.getGuestId(),
                room.getCategoryId(),
                room.getStatus(),
                room.getAssigneeId(),
                room.getCreatedAt(),
                room.getClosedAt());
    }
}
