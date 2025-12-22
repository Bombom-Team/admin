package me.bombom.api.v1.notice.dto;

import me.bombom.api.v1.notice.domain.NoticeCategory;

public record UpdateNoticeRequest(

        String title,
        String content,
        NoticeCategory noticeCategory
) {
}
