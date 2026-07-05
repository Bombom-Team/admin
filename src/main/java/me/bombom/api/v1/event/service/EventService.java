package me.bombom.api.v1.event.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.event.Event;
import me.bombom.api.v1.event.dto.CreateEventRequest;
import me.bombom.api.v1.event.dto.GetEventDetailResponse;
import me.bombom.api.v1.event.dto.GetEventResponse;
import me.bombom.api.v1.event.dto.GetEventsRequest;
import me.bombom.api.v1.event.dto.UpdateEventRequest;
import me.bombom.api.v1.event.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public Page<GetEventResponse> getEvents(GetEventsRequest request, Pageable pageable) {
        return eventRepository.findEvents(request, pageable);
    }

    public GetEventDetailResponse getEvent(Long id) {
        return eventRepository.findById(id)
                .map(GetEventDetailResponse::from)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "event")
                        .addContext(ErrorContextKeys.OPERATION, "getEvent"));
    }

    @Transactional
    public void createEvent(CreateEventRequest request) {
        Event event = Event.builder()
                .name(request.name())
                .startTime(request.startTime())
                .status(request.status())
                .build();
        eventRepository.save(event);
    }

    @Transactional
    public void updateEvent(Long id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "event")
                        .addContext(ErrorContextKeys.OPERATION, "updateEvent"));

        event.update(request.name(), request.startTime(), request.status());
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "event")
                    .addContext(ErrorContextKeys.OPERATION, "deleteEvent");
        }
        eventRepository.deleteById(id);
    }
}
