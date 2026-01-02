package me.bombom.api.v1.challenge.dto;

import java.time.LocalDate;
import lombok.Builder;
import me.bombom.api.v1.challenge.domain.Challenge;

@Builder
public record GetChallengeDetailResponse(
        Long id,
        String name,
        int generation,
        LocalDate startDate,
        LocalDate endDate
) {

    public static GetChallengeDetailResponse from(Challenge challenge) {
        return GetChallengeDetailResponse.builder()
                .id(challenge.getId())
                .name(challenge.getName())
                .generation(challenge.getGeneration())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .build();
    }
}
