package me.bombom.api.v1.holiday.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateHolidayRequest(

        @NotNull
        LocalDate date,

        @NotBlank
        @Size(max = 255)
        String name
) {
}
