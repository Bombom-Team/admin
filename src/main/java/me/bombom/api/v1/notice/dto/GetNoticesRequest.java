package me.bombom.api.v1.notice.dto;

import me.bombom.api.v1.notice.domain.NoticeCategory;

public record GetNoticesRequest(

        String keyword,
        NoticeCategory category
) {
}
