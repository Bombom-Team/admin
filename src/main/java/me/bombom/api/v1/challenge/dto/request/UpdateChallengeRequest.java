package me.bombom.api.v1.challenge.dto.request;

import java.time.LocalDate;

public record UpdateChallengeRequest(

        String name,

        Integer generation,

        LocalDate startDate,

        LocalDate endDate
) {
}
