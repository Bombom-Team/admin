package me.bombom.api.v1.event.repository;

import me.bombom.api.v1.event.dto.GetEventResponse;
import me.bombom.api.v1.event.dto.GetEventsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomEventRepository {

    Page<GetEventResponse> findEvents(GetEventsRequest request, Pageable pageable);
}

