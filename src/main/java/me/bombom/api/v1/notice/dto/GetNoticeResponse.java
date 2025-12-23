package me.bombom.api.v1.notice.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import me.bombom.api.v1.notice.domain.NoticeCategory;

public record GetNoticeResponse(
                Long id,
                String title,
                String noticeCategory,
                LocalDate createdAt
) {

        @QueryProjection
        public GetNoticeResponse(Long id, String title, String noticeCategory,
                        LocalDateTime createdAt) {
                this(id, title, NoticeCategory.valueOf(noticeCategory).getValue(),
                                createdAt != null ? createdAt.toLocalDate() : null);
        }
}
