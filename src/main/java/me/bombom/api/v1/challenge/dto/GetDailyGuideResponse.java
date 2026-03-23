package me.bombom.api.v1.challenge.dto;

import me.bombom.api.v1.challenge.domain.DailyGuideType;

public record GetDailyGuideResponse(
        Long id,
        Long challengeId,
        int dayIndex,
        DailyGuideType type,
        String imageUrl,
        String notice,
        boolean commentEnabled
) {
}
