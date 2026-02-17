package me.bombom.api.v1.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.event.dto.CreateEventRequest;
import me.bombom.api.v1.event.dto.GetEventDetailResponse;
import me.bombom.api.v1.event.dto.GetEventResponse;
import me.bombom.api.v1.event.dto.GetEventsRequest;
import me.bombom.api.v1.event.dto.UpdateEventRequest;
import me.bombom.api.v1.event.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
public class EventController implements EventControllerApi {

    private final EventService eventService;

    @Override
    @GetMapping
    public Page<GetEventResponse> getEvents(
            @ModelAttribute GetEventsRequest request,
            @PageableDefault(sort = "id", direction = Direction.ASC) Pageable pageable
    ) {
        return eventService.getEvents(request, pageable);
    }

    @Override
    @GetMapping("/{id}")
    public GetEventDetailResponse getEvent(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        return eventService.getEvent(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createEvent(@Valid @RequestBody CreateEventRequest request) {
        eventService.createEvent(request);
    }

    @Override
    @PatchMapping("/{id}")
    public void updateEvent(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @RequestBody UpdateEventRequest request
    ) {
        eventService.updateEvent(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        eventService.deleteEvent(id);
    }
}

