package me.bombom.api.v1.notice.dto;

import java.util.Objects;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.domain.NoticeVisibility;

import java.util.List;

public record UpdateNoticeRequest(

        String title,
        String content,
        NoticeCategory noticeCategory,
        NoticeVisibility visibility,
        Boolean isRepresentative,
        List<Long> referencedImageIds
) {

    public void validate() {
        validateReferencedImageIds();
    }

    public List<Long> distinctReferencedImageIds() {
        if (referencedImageIds == null) {
            return List.of();
        }

        return referencedImageIds.stream()
                .distinct()
                .toList();
    }

    private void validateReferencedImageIds() {
        if (referencedImageIds == null) {
            return;
        }

        boolean hasNullImageId = referencedImageIds.stream()
                .anyMatch(Objects::isNull);

        if (hasNullImageId) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("field", "referencedImageIds");
        }
    }
}
