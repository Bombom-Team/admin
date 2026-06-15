package me.bombom.api.v1.holiday.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GetHolidaysRequest(
        @NotNull(message = "year는 필수입니다.")
        @Min(value = 1900, message = "year는 1900 이상이어야 합니다.")
        @Max(value = 9999, message = "year는 9999 이하이어야 합니다.")
        Integer year
) {

    public LocalDate startDate() {
        return LocalDate.of(year, 1, 1);
    }

    public LocalDate endDate() {
        return LocalDate.of(year, 12, 31);
    }
}
