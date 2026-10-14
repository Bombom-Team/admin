package me.bombom.api.v1.holiday.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateHolidayRequest(

        LocalDate date,

        @Size(max = 255)
        String name
) {
}
