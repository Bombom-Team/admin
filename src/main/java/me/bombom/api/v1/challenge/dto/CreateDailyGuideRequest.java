package me.bombom.api.v1.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.DailyGuideType;

public record CreateDailyGuideRequest(
        @NotNull
        @Min(1)
        Integer dayIndex,

        @NotNull
        DailyGuideType type,

        @NotBlank
        @Size(max = 2048)
        String imageUrl,

        @Size(max = 1000)
        String notice,

        @NotNull
        Boolean commentEnabled
) {

    public ChallengeDailyGuide toEntity(Long challengeId) {
        return ChallengeDailyGuide.builder()
                .challengeId(challengeId)
                .dayIndex(dayIndex)
                .type(type)
                .imageUrl(imageUrl)
                .notice(notice)
                .commentEnabled(commentEnabled)
                .build();
    }
}
