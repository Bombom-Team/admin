package me.bombom.api.v1.challenge.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record GetChallengeResponse(
        Long id,
        String name,
        int generation,
        LocalDate startDate,
        LocalDate endDate
) {

    @QueryProjection
    public GetChallengeResponse {
    }
}
