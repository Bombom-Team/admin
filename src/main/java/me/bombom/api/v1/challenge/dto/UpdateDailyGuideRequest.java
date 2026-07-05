package me.bombom.api.v1.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.challenge.dto.validation.CommentGuideValidatable;
import me.bombom.api.v1.challenge.dto.validation.ValidCommentGuide;

@ValidCommentGuide
public record UpdateDailyGuideRequest(
        @Min(1)
        Integer dayIndex,

        DailyGuideType type,

        String fileName,

        String imageUrl,

        @Size(max = 1000)
        String notice
) implements CommentGuideValidatable {
}
