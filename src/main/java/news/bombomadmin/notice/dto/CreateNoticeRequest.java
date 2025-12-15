package news.bombomadmin.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import news.bombomadmin.notice.domain.NoticeCategory;

public record CreateNoticeRequest(

        @NotBlank
        String title,

        @NotBlank
        String content,

        @NotNull
        NoticeCategory noticeCategory
) {
}
