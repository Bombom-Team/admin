package me.bombom.api.v1.event.repository;

import me.bombom.api.v1.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long>, CustomEventRepository {
}


