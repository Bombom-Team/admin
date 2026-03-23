package me.bombom.api.v1.challenge.dto;

import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
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

    public static GetDailyGuideResponse from(ChallengeDailyGuide guide) {
        return new GetDailyGuideResponse(
                guide.getId(),
                guide.getChallengeId(),
                guide.getDayIndex(),
                guide.getType(),
                guide.getImageUrl(),
                guide.getNotice(),
                guide.isCommentEnabled()
        );
    }
}
