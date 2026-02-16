package me.bombom.api.v1.event.dto;

import java.time.LocalDateTime;
import me.bombom.api.v1.event.Event;
import me.bombom.api.v1.event.EventStatus;

public record GetEventResponse(

        Long id,
        String name,
        LocalDateTime startTime,
        EventStatus status
) {

    public static GetEventResponse from(Event event) {
        return new GetEventResponse(
                event.getId(),
                event.getName(),
                event.getStartTime(),
                event.getStatus());
    }
}


