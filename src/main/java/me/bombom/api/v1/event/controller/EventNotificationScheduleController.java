package me.bombom.api.v1.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.event.dto.CreateEventNotificationScheduleRequest;
import me.bombom.api.v1.event.dto.GetEventNotificationScheduleResponse;
import me.bombom.api.v1.event.dto.UpdateEventNotificationScheduleRequest;
import me.bombom.api.v1.event.service.EventNotificationScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/events")
public class EventNotificationScheduleController implements EventNotificationScheduleControllerApi {

    private final EventNotificationScheduleService scheduleService;

    @Override
    @GetMapping("/{id}/schedules")
    public List<GetEventNotificationScheduleResponse> getEventNotificationSchedules(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        return scheduleService.getSchedules(id);
    }

    @Override
    @GetMapping("/{id}/schedules/{scheduleId}")
    public GetEventNotificationScheduleResponse getEventNotificationSchedule(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long scheduleId
    ) {
        return scheduleService.getSchedule(id, scheduleId);
    }

    @Override
    @PostMapping("/{id}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public void createEventNotificationSchedule(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Valid @RequestBody CreateEventNotificationScheduleRequest request
    ) {
        scheduleService.createSchedule(id, request);
    }

    @Override
    @PatchMapping("/{id}/schedules/{scheduleId}")
    public void updateEventNotificationSchedule(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long scheduleId,
            @Valid @RequestBody UpdateEventNotificationScheduleRequest request
    ) {
        scheduleService.updateSchedule(id, scheduleId, request);
    }

    @Override
    @DeleteMapping("/{id}/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEventNotificationSchedule(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long scheduleId
    ) {
        scheduleService.deleteSchedule(id, scheduleId);
    }
}
