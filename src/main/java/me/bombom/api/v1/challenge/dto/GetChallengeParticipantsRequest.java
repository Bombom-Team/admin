package me.bombom.api.v1.challenge.dto;

public record GetChallengeParticipantsRequest(

        Long challengeTeamId,
        Boolean hasTeam,
        Boolean isSurvived
) {
}
