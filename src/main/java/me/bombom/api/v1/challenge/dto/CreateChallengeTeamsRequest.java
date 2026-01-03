package me.bombom.api.v1.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateChallengeTeamsRequest(

        @NotNull @Min(1) Integer count
) {
}
