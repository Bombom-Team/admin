package me.bombom.api.v1.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.event.Event;
import me.bombom.api.v1.event.EventStatus;
import me.bombom.api.v1.event.dto.CreateEventRequest;
import me.bombom.api.v1.event.dto.GetEventDetailResponse;
import me.bombom.api.v1.event.dto.GetEventResponse;
import me.bombom.api.v1.event.dto.GetEventsRequest;
import me.bombom.api.v1.event.dto.UpdateEventRequest;
import me.bombom.api.v1.event.fixture.EventFixture;
import me.bombom.api.v1.event.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@EnableJpaAuditing
@Import({ EventService.class, QuerydslConfig.class })
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("이벤트를 생성한다.")
    void 이벤트를_생성한다() {
        // given
        CreateEventRequest request = new CreateEventRequest(
                "테스트 이벤트",
                LocalDateTime.now(),
                EventStatus.SCHEDULED);

        // when
        eventService.createEvent(request);

        // then
        List<Event> events = eventRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(events).hasSize(1);
            softly.assertThat(events.getFirst().getName()).isEqualTo("테스트 이벤트");
            softly.assertThat(events.getFirst().getStatus()).isEqualTo(EventStatus.SCHEDULED);
        });
    }

    @Test
    @DisplayName("이벤트 상세를 조회한다.")
    void 이벤트_상세를_조회한다() {
        // given
        Event saved = eventRepository.save(EventFixture.createEvent("상세 이벤트", EventStatus.SCHEDULED));

        // when
        GetEventDetailResponse response = eventService.getEvent(saved.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.id()).isEqualTo(saved.getId());
            softly.assertThat(response.name()).isEqualTo("상세 이벤트");
            softly.assertThat(response.status()).isEqualTo(EventStatus.SCHEDULED);
        });
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 상세 조회 시 예외가 발생한다.")
    void 존재하지_않는_이벤트_상세_조회시_예외_발생() {
        // when & then
        assertThatThrownBy(() -> eventService.getEvent(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이벤트 목록을 조회한다.")
    void 이벤트_목록을_조회한다() {
        // given
        eventRepository.save(EventFixture.createEvent("테스트 이벤트1", EventStatus.SCHEDULED));
        eventRepository.save(EventFixture.createEvent("다른 이벤트", EventStatus.IN_PROGRESS));

        GetEventsRequest request = new GetEventsRequest("테스트", null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<GetEventResponse> result = eventService.getEvents(request, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("테스트 이벤트1");
    }

    @Test
    @DisplayName("이벤트 전체 정보를 수정한다.")
    void 이벤트_전체_정보를_수정한다() {
        // given
        Event saved = eventRepository.save(EventFixture.createEvent("상태 이벤트", EventStatus.SCHEDULED));

        UpdateEventRequest request = new UpdateEventRequest(
                "수정된 이벤트",
                LocalDateTime.now().plusDays(1),
                EventStatus.IN_PROGRESS);

        // when
        eventService.updateEvent(saved.getId(), request);

        // then
        Event updated = eventRepository.findById(saved.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getName()).isEqualTo("수정된 이벤트");
            softly.assertThat(updated.getStatus()).isEqualTo(EventStatus.IN_PROGRESS);
        });
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 수정 시 예외가 발생한다.")
    void 존재하지_않는_이벤트_수정시_예외_발생() {
        // given
        UpdateEventRequest request = new UpdateEventRequest(
                "수정된 이벤트",
                LocalDateTime.now().plusDays(1),
                EventStatus.IN_PROGRESS);

        // when & then
        assertThatThrownBy(() -> eventService.updateEvent(999L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이벤트를 삭제한다.")
    void 이벤트를_삭제한다() {
        // given
        Event saved = eventRepository.save(EventFixture.createEvent("삭제 이벤트", EventStatus.SCHEDULED));

        // when
        eventService.deleteEvent(saved.getId());

        // then
        assertThat(eventRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 삭제 시 예외가 발생한다.")
    void 존재하지_않는_이벤트_삭제시_예외_발생() {
        // when & then
        assertThatThrownBy(() -> eventService.deleteEvent(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }
}
