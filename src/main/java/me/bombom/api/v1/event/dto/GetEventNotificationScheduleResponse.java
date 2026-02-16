package me.bombom.api.v1.event.dto;

import java.time.LocalDateTime;
import me.bombom.api.v1.event.EventNotificationSchedule;
import me.bombom.api.v1.event.NotificationScheduleType;

public record GetEventNotificationScheduleResponse(

        Long id,
        LocalDateTime scheduledAt,
        NotificationScheduleType type,
        Integer minutesBefore,
        boolean sent,
        LocalDateTime sentAt
) {

    public static GetEventNotificationScheduleResponse from(EventNotificationSchedule schedule) {
        return new GetEventNotificationScheduleResponse(
                schedule.getId(),
                schedule.getScheduledAt(),
                schedule.getType(),
                schedule.getMinutesBefore(),
                schedule.isSent(),
                schedule.getSentAt());
    }
}
