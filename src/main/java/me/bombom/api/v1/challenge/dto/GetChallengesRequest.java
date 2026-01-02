package me.bombom.api.v1.challenge.dto;

import me.bombom.api.v1.challenge.domain.ChallengeStatus;

public record GetChallengesRequest(
        ChallengeStatus status
) {
}
