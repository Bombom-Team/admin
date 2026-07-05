package me.bombom.api.v1.event.dto;

import me.bombom.api.v1.event.EventStatus;

public record GetEventsRequest(

        String keyword,
        EventStatus status
) {
}
