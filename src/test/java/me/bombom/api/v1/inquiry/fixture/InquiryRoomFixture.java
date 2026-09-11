package me.bombom.api.v1.inquiry.fixture;

import me.bombom.api.v1.inquiry.domain.InquiryRoom;

public class InquiryRoomFixture {

    public static InquiryRoom createMemberRoom(Long memberId, Long categoryId) {
        return InquiryRoom.createMemberInquiryRoom(memberId, categoryId);
    }

    public static InquiryRoom createGuestRoom(String guestId, Long categoryId) {
        return InquiryRoom.createGuestInquiryRoom(guestId, categoryId);
    }
}
