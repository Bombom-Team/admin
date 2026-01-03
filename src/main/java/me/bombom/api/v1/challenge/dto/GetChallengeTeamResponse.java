package me.bombom.api.v1.challenge.dto;

import lombok.Builder;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;

@Builder
public record GetChallengeTeamResponse(
        Long id,
        Long challengeId,
        int progress
) {

    public static GetChallengeTeamResponse from(ChallengeTeam team) {
        return GetChallengeTeamResponse.builder()
                .id(team.getId())
                .challengeId(team.getChallengeId())
                .progress(team.getProgress())
                .build();
    }
}
