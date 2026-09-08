package me.bombom.api.v1.holiday.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateHolidaysRequest(

        @Valid
        @NotEmpty
        @Size(max = 100)
        List<CreateHolidayRequest> holidays
) {
}
