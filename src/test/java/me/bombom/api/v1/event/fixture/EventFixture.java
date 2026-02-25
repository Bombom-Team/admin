package me.bombom.api.v1.event.fixture;

import static org.instancio.Select.field;

import java.time.LocalDateTime;
import me.bombom.api.v1.event.Event;
import me.bombom.api.v1.event.EventNotificationSchedule;
import me.bombom.api.v1.event.EventStatus;
import me.bombom.api.v1.event.NotificationScheduleType;
import org.instancio.Instancio;

public class EventFixture {

    public static Event createEvent(String name, EventStatus status) {
        return Instancio.of(Event.class)
                .set(field(Event::getId), null)
                .set(field(Event::getName), name)
                .set(field(Event::getStartTime), LocalDateTime.now())
                .set(field(Event::getStatus), status)
                .create();
    }

    public static EventNotificationSchedule createNotificationSchedule(Long eventId, NotificationScheduleType type) {
        return Instancio.of(EventNotificationSchedule.class)
                .set(field(EventNotificationSchedule::getId), null)
                .set(field(EventNotificationSchedule::getEventId), eventId)
                .set(field(EventNotificationSchedule::getType), type)
                .set(field(EventNotificationSchedule::getScheduledAt), LocalDateTime.now())
                .set(field(EventNotificationSchedule::isSent), false)
                .create();
    }
}
