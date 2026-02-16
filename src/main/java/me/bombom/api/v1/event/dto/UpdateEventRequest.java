package me.bombom.api.v1.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.event.EventStatus;

public record UpdateEventRequest(

        @NotBlank
        String name,

        @NotNull
        LocalDateTime startTime,

        @NotNull
        EventStatus status
) {
}
