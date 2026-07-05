package me.bombom.api.v1.challenge.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.DailyGuideType;

public record GetChallengeDayResponse(

        LocalDate date,
        DayOfWeek dayOfWeek,
        int dayIndex,
        DailyGuideType dailyGuideType,
        String imageUrl
) {
}
