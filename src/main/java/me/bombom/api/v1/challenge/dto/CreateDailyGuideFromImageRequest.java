package me.bombom.api.v1.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.challenge.dto.validation.CommentGuideValidatable;
import me.bombom.api.v1.challenge.dto.validation.ValidCommentGuide;

@ValidCommentGuide
public record CreateDailyGuideFromImageRequest(
        @NotNull
        @Min(1)
        Integer dayIndex,

        @NotNull
        DailyGuideType type,

        @NotBlank
        String imageUrl,

        @Size(max = 1000)
        String notice
) implements CommentGuideValidatable {

    public ChallengeDailyGuide toEntity(Long challengeId) {
        return ChallengeDailyGuide.builder()
                .challengeId(challengeId)
                .dayIndex(dayIndex)
                .type(type)
                .imageUrl(imageUrl)
                .notice(notice)
                .commentEnabled(type == DailyGuideType.COMMENT)
                .build();
    }
}
