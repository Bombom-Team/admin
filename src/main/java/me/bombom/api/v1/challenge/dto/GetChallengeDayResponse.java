package me.bombom.api.v1.challenge.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;

public record GetChallengeDayResponse(

        LocalDate date,
        DayOfWeek dayOfWeek,
        int dayIndex
) {
}
