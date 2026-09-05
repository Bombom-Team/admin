package me.bombom.api.v1.notice.dto;

import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.domain.NoticeVisibility;

public record UpdateNoticeRequest(

        String title,
        String content,
        NoticeCategory noticeCategory,
        NoticeVisibility visibility,
        Boolean isRepresentative
) {
}
