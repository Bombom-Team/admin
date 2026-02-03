package me.bombom.api.v1.challenge.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record GetChallengeParticipantResponse(
        Long participantId,
        String nickname,
        Long challengeTeamId,
        int completedDays,
        boolean isSurvived,
        int shield
) {

        @QueryProjection
        public GetChallengeParticipantResponse {
        }
}
