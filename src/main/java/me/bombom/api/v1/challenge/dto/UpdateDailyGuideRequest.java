package me.bombom.api.v1.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.challenge.domain.DailyGuideType;

public record UpdateDailyGuideRequest(
        @Min(1)
        Integer dayIndex,

        DailyGuideType type,

        @Size(max = 2048)
        String imageUrl,

        @Size(max = 1000)
        String notice,

        Boolean commentEnabled
) {
}
