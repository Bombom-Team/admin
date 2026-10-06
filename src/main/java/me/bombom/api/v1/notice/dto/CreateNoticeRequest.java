package me.bombom.api.v1.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.notice.domain.NoticeCategory;

public record CreateNoticeRequest(

        @NotBlank
        String title,

        @NotBlank
        String content,

        @NotNull
        NoticeCategory noticeCategory
) {
}
