package me.bombom.api.v1.event.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.event.EventNotificationSchedule;
import me.bombom.api.v1.event.NotificationScheduleType;
import me.bombom.api.v1.event.dto.CreateEventNotificationScheduleRequest;
import me.bombom.api.v1.event.dto.GetEventNotificationScheduleResponse;
import me.bombom.api.v1.event.dto.UpdateEventNotificationScheduleRequest;
import me.bombom.api.v1.event.repository.EventNotificationScheduleRepository;
import me.bombom.api.v1.event.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventNotificationScheduleService {

    private final EventRepository eventRepository;
    private final EventNotificationScheduleRepository scheduleRepository;

    public List<GetEventNotificationScheduleResponse> getSchedules(Long eventId) {
        validateEventExists(eventId);
        return scheduleRepository.findByEventIdOrderByScheduledAtAscIdAsc(eventId).stream()
                .map(GetEventNotificationScheduleResponse::from)
                .toList();
    }

    public GetEventNotificationScheduleResponse getSchedule(Long eventId, Long scheduleId) {
        validateEventExists(eventId);
        EventNotificationSchedule schedule = scheduleRepository.findByIdAndEventId(scheduleId, eventId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "eventNotificationSchedule")
                        .addContext(ErrorContextKeys.OPERATION, "getSchedule"));
        return GetEventNotificationScheduleResponse.from(schedule);
    }

    @Transactional
    public void createSchedule(Long eventId, CreateEventNotificationScheduleRequest request) {
        validateEventExists(eventId);
        validateMinutesBefore(request.type(), request.minutesBefore());

        EventNotificationSchedule schedule = EventNotificationSchedule.builder()
                .eventId(eventId)
                .scheduledAt(request.scheduledAt())
                .type(request.type())
                .minutesBefore(request.minutesBefore())
                .build();
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void updateSchedule(Long eventId, Long scheduleId, UpdateEventNotificationScheduleRequest request) {
        validateEventExists(eventId);
        validateMinutesBefore(request.type(), request.minutesBefore());
        EventNotificationSchedule schedule = scheduleRepository.findByIdAndEventId(scheduleId, eventId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "eventNotificationSchedule")
                        .addContext(ErrorContextKeys.OPERATION, "updateSchedule"));

        schedule.update(
                request.scheduledAt(),
                request.type(),
                request.minutesBefore());
    }

    @Transactional
    public void deleteSchedule(Long eventId, Long scheduleId) {
        validateEventExists(eventId);

        scheduleRepository.delete(
                scheduleRepository.findByIdAndEventId(scheduleId, eventId)
                        .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                                .addContext(ErrorContextKeys.ENTITY_TYPE, "eventNotificationSchedule")
                                .addContext(ErrorContextKeys.OPERATION, "deleteSchedule")));
    }

    private void validateEventExists(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "event")
                    .addContext(ErrorContextKeys.OPERATION, "eventNotificationSchedule");
        }
    }

    private void validateMinutesBefore(NotificationScheduleType type, Integer minutesBefore) {
        if (type == NotificationScheduleType.AT_START && minutesBefore != null) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.REASON, "AT_START 타입에서는 minutesBefore를 설정할 수 없습니다.");
        }
    }
}
