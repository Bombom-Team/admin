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
import me.bombom.api.v1.event.EventNotificationSchedule;
import me.bombom.api.v1.event.EventStatus;
import me.bombom.api.v1.event.NotificationScheduleType;
import me.bombom.api.v1.event.dto.CreateEventNotificationScheduleRequest;
import me.bombom.api.v1.event.dto.GetEventNotificationScheduleResponse;
import me.bombom.api.v1.event.dto.UpdateEventNotificationScheduleRequest;
import me.bombom.api.v1.event.fixture.EventFixture;
import me.bombom.api.v1.event.repository.EventNotificationScheduleRepository;
import me.bombom.api.v1.event.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@Import({ EventNotificationScheduleService.class, QuerydslConfig.class })
@EnableJpaAuditing
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class EventNotificationScheduleServiceTest {

        @Autowired
        private EventNotificationScheduleService scheduleService;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private EventNotificationScheduleRepository scheduleRepository;

        @Test
        @DisplayName("이벤트에 대한 알림 스케줄을 생성한다.")
        void 이벤트_알림_스케줄을_생성한다() {
                // given
                Event event = eventRepository.save(EventFixture.createEvent("이벤트", EventStatus.SCHEDULED));

                CreateEventNotificationScheduleRequest request = new CreateEventNotificationScheduleRequest(
                                LocalDateTime.now().plusMinutes(10),
                                NotificationScheduleType.BEFORE_MINUTES,
                                10);

                // when
                scheduleService.createSchedule(event.getId(), request);

                // then
                List<EventNotificationSchedule> schedules = scheduleRepository.findByEventIdOrderByScheduledAtAscIdAsc(
                                event.getId());
                assertSoftly(softly -> {
                        softly.assertThat(schedules).hasSize(1);
                        softly.assertThat(schedules.getFirst().getMinutesBefore()).isEqualTo(10);
                        softly.assertThat(schedules.getFirst().getType())
                                        .isEqualTo(NotificationScheduleType.BEFORE_MINUTES);
                });
        }

        @Test
        @DisplayName("AT_START 타입에서 minutesBefore가 있으면 생성 시 예외가 발생한다.")
        void at_start_타입_minutesBefore_있으면_생성_예외() {
                // given
                Event event = eventRepository.save(EventFixture.createEvent("이벤트", EventStatus.SCHEDULED));

                CreateEventNotificationScheduleRequest request = new CreateEventNotificationScheduleRequest(
                                LocalDateTime.now().plusMinutes(10),
                                NotificationScheduleType.AT_START,
                                10);

                // when & then
                assertThatThrownBy(() -> scheduleService.createSchedule(event.getId(), request))
                                .isInstanceOf(CIllegalArgumentException.class)
                                .hasMessage(ErrorDetail.INVALID_INPUT_VALUE.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 이벤트에 대한 스케줄 생성 시 예외가 발생한다.")
        void 존재하지_않는_이벤트_스케줄_생성시_예외() {
                // given
                CreateEventNotificationScheduleRequest request = new CreateEventNotificationScheduleRequest(
                                LocalDateTime.now().plusMinutes(10),
                                NotificationScheduleType.BEFORE_MINUTES,
                                10);

                // when & then
                assertThatThrownBy(() -> scheduleService.createSchedule(999L, request))
                                .isInstanceOf(CIllegalArgumentException.class)
                                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이벤트에 대한 알림 스케줄 목록을 조회한다.")
        void 이벤트_알림_스케줄_목록을_조회한다() {
                // given
                Event event = eventRepository.save(EventFixture.createEvent("이벤트", EventStatus.SCHEDULED));

                scheduleRepository.save(EventFixture.createNotificationSchedule(event.getId(),
                                NotificationScheduleType.BEFORE_MINUTES));
                scheduleRepository.save(EventFixture.createNotificationSchedule(event.getId(),
                                NotificationScheduleType.AT_START));

                // when
                List<GetEventNotificationScheduleResponse> result = scheduleService.getSchedules(event.getId());

                // then
                assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("이벤트에 대한 단일 알림 스케줄을 조회한다.")
        void 이벤트_단일_알림_스케줄을_조회한다() {
                // given
                Event event = eventRepository.save(EventFixture.createEvent("이벤트", EventStatus.SCHEDULED));

                EventNotificationSchedule schedule = scheduleRepository.save(EventFixture
                                .createNotificationSchedule(event.getId(), NotificationScheduleType.BEFORE_MINUTES));

                // when
                GetEventNotificationScheduleResponse response = scheduleService.getSchedule(event.getId(),
                                schedule.getId());

                // then
                assertSoftly(softly -> {
                        softly.assertThat(response.id()).isEqualTo(schedule.getId());
                        softly.assertThat(response.type()).isEqualTo(NotificationScheduleType.BEFORE_MINUTES);
                });
        }

        @Test
        @DisplayName("다른 이벤트의 스케줄을 조회할 경우 예외가 발생한다.")
        void 다른_이벤트_스케줄_조회시_예외() {
                // given
                Event event1 = eventRepository.save(EventFixture.createEvent("이벤트1", EventStatus.SCHEDULED));
                Event event2 = eventRepository.save(EventFixture.createEvent("이벤트2", EventStatus.SCHEDULED));

                EventNotificationSchedule schedule = scheduleRepository.save(EventFixture
                                .createNotificationSchedule(event2.getId(), NotificationScheduleType.BEFORE_MINUTES));

                // when & then
                assertThatThrownBy(() -> scheduleService.getSchedule(event1.getId(), schedule.getId()))
                                .isInstanceOf(CIllegalArgumentException.class)
                                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이벤트에 대한 알림 스케줄을 수정한다.")
        void 이벤트_알림_스케줄을_수정한다() {
                // given
                Event event = eventRepository.save(EventFixture.createEvent("이벤트", EventStatus.SCHEDULED));

                EventNotificationSchedule schedule = scheduleRepository.save(EventFixture
                                .createNotificationSchedule(event.getId(), NotificationScheduleType.BEFORE_MINUTES));

                UpdateEventNotificationScheduleRequest request = new UpdateEventNotificationScheduleRequest(
                                LocalDateTime.now().plusMinutes(15),
                                NotificationScheduleType.AT_START,
                                null);

                // when
                scheduleService.updateSchedule(event.getId(), schedule.getId(), request);

                // then
                EventNotificationSchedule updated = scheduleRepository.findById(schedule.getId()).orElseThrow();
                assertSoftly(softly -> {
                        softly.assertThat(updated.getType()).isEqualTo(NotificationScheduleType.AT_START);
                        softly.assertThat(updated.getMinutesBefore()).isNull();
                });
        }

        @Test
        @DisplayName("AT_START 타입에서 minutesBefore가 있으면 수정 시 예외가 발생한다.")
        void at_start_타입_minutesBefore_있으면_수정_예외() {
                // given
                Event event = eventRepository.save(EventFixture.createEvent("이벤트", EventStatus.SCHEDULED));

                EventNotificationSchedule schedule = scheduleRepository.save(EventFixture
                                .createNotificationSchedule(event.getId(), NotificationScheduleType.BEFORE_MINUTES));

                UpdateEventNotificationScheduleRequest request = new UpdateEventNotificationScheduleRequest(
                                LocalDateTime.now().plusMinutes(15),
                                NotificationScheduleType.AT_START,
                                10);

                // when & then
                assertThatThrownBy(() -> scheduleService.updateSchedule(event.getId(), schedule.getId(), request))
                                .isInstanceOf(CIllegalArgumentException.class)
                                .hasMessage(ErrorDetail.INVALID_INPUT_VALUE.getMessage());
        }

        @Test
        @DisplayName("이벤트에 대한 알림 스케줄을 삭제한다.")
        void 이벤트_알림_스케줄을_삭제한다() {
                // given
                Event event = eventRepository.save(EventFixture.createEvent("이벤트", EventStatus.SCHEDULED));

                EventNotificationSchedule schedule = scheduleRepository.save(EventFixture
                                .createNotificationSchedule(event.getId(), NotificationScheduleType.BEFORE_MINUTES));

                // when
                scheduleService.deleteSchedule(event.getId(), schedule.getId());

                // then
                assertThat(scheduleRepository.existsById(schedule.getId())).isFalse();
        }
}
