package me.bombom.api.v1.challenge.dto;

public record GetChallengeParticipantsRequest(

        Long challengeTeamId,
        boolean hasTeam
) {
}
