package me.bombom.api.v1.holiday.dto;

import java.time.LocalDate;
import me.bombom.api.v1.holiday.domain.Holiday;

public record GetHolidayResponse(

        Long id,
        LocalDate date,
        String name
) {

    public static GetHolidayResponse from(Holiday holiday) {
        return new GetHolidayResponse(
                holiday.getId(),
                holiday.getDate(),
                holiday.getName());
    }
}
