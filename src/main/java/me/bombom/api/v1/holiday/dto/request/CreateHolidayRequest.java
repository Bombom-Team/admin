package me.bombom.api.v1.holiday.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateHolidayRequest(
        @NotNull(message = "date는 필수입니다.")
        LocalDate date,

        @NotBlank(message = "name은 필수입니다.")
        String name
) {
}
