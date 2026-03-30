package me.bombom.api.v1.challenge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateChallengeRequest(

        @NotBlank
        String name,

        @Positive
        int generation,

        LocalDate startDate,

        LocalDate endDate,

        @NotNull
        Long newsletterGroupId
) {
}
