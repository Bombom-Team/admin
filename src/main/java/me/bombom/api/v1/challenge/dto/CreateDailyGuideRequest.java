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
public record CreateDailyGuideRequest(
        @NotNull
        @Min(1)
        Integer dayIndex,

        @NotNull
        DailyGuideType type,

        @NotBlank
        String fileName,

        @Size(max = 1000)
        String notice
) implements CommentGuideValidatable {

    public ChallengeDailyGuide toEntity(Long challengeId, String imageUrl) {
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
