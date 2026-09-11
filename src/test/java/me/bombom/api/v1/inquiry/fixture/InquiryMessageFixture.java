package me.bombom.api.v1.inquiry.fixture;

import me.bombom.api.v1.inquiry.domain.InquiryMessage;

public class InquiryMessageFixture {

    public static InquiryMessage createAdminMessage(Long roomId, Long adminId, String content) {
        return InquiryMessage.createAdminMessage(roomId, adminId, content);
    }
}
