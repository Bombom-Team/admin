package me.bombom.api.v1.event.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.event.NotificationScheduleType;

public record CreateEventNotificationScheduleRequest(

        @NotNull
        LocalDateTime scheduledAt,

        @NotNull
        NotificationScheduleType type,

        Integer minutesBefore
) {
}
