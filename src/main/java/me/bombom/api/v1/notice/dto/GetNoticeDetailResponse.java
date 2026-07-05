package me.bombom.api.v1.notice.dto;

import java.time.LocalDate;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;

public record GetNoticeDetailResponse(
        String title,
        NoticeCategory noticeCategory,
        String content,
        LocalDate createdAt
) {

    public static GetNoticeDetailResponse from(Notice notice) {
        return new GetNoticeDetailResponse(
                notice.getTitle(),
                notice.getNoticeCategory(),
                notice.getContent(),
                notice.getCreatedAt().toLocalDate());
    }
}
