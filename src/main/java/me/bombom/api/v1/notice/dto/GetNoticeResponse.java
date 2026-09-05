package me.bombom.api.v1.notice.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.domain.NoticeVisibility;

public record GetNoticeResponse(
                Long id,
                String title,
                NoticeCategory noticeCategory,
                NoticeVisibility visibility,
                boolean isRepresentative,
                LocalDate createdAt
) {

        @QueryProjection
        public GetNoticeResponse(
                Long id, 
                String title, 
                NoticeCategory noticeCategory,
                NoticeVisibility visibility, 
                boolean isRepresentative, 
                LocalDateTime createdAt
        ) {
                this(id, title, noticeCategory, visibility, isRepresentative,
                                createdAt != null ? createdAt.toLocalDate() : null);
        }
}
