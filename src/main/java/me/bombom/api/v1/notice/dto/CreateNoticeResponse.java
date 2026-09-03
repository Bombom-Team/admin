package me.bombom.api.v1.notice.dto;

import me.bombom.api.v1.notice.domain.Notice;

public record CreateNoticeResponse(
        Long noticeId
) {

    public static CreateNoticeResponse from(Notice notice) {
        return new CreateNoticeResponse(notice.getId());
    }
}
