package me.bombom.api.v1.event.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.event.EventNotificationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventNotificationScheduleRepository extends JpaRepository<EventNotificationSchedule, Long> {

    List<EventNotificationSchedule> findByEventIdOrderByScheduledAtAscIdAsc(Long eventId);

    Optional<EventNotificationSchedule> findByIdAndEventId(Long id, Long eventId);
}
