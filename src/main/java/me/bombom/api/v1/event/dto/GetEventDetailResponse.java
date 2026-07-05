package me.bombom.api.v1.event.dto;

import java.time.LocalDateTime;
import me.bombom.api.v1.event.Event;
import me.bombom.api.v1.event.EventStatus;

public record GetEventDetailResponse(

        Long id,
        String name,
        LocalDateTime startTime,
        EventStatus status
) {

    public static GetEventDetailResponse from(Event event) {
        return new GetEventDetailResponse(
                event.getId(),
                event.getName(),
                event.getStartTime(),
                event.getStatus()
        );
    }
}
