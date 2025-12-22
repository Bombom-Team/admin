package me.bombom.api.v1.notice.dto;

import com.querydsl.core.annotations.QueryProjection;
import me.bombom.api.v1.notice.domain.NoticeCategory;

public record GetNoticeResponse(
                Long id,
                String title,
                String content,
                String noticeCategory
) {

        @QueryProjection
        public GetNoticeResponse(Long id, String title, String content, String noticeCategory) {
                this.id = id;
                this.title = title;
                this.content = content;
                this.noticeCategory = NoticeCategory.valueOf(noticeCategory).getValue();
        }
}
