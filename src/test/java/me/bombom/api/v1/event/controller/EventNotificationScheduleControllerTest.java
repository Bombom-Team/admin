package me.bombom.api.v1.event.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.event.NotificationScheduleType;
import me.bombom.api.v1.event.dto.CreateEventNotificationScheduleRequest;
import me.bombom.api.v1.event.dto.GetEventNotificationScheduleResponse;
import me.bombom.api.v1.event.dto.UpdateEventNotificationScheduleRequest;
import me.bombom.api.v1.event.service.EventNotificationScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = EventNotificationScheduleController.class)
class EventNotificationScheduleControllerTest extends ControllerTestSupport {

    @MockitoBean
    private EventNotificationScheduleService scheduleService;

    @Test
    @DisplayName("이벤트에 대한 알림 스케줄을 생성한다.")
    void 이벤트_알림_스케줄을_생성한다() throws Exception {
        // given
        CreateEventNotificationScheduleRequest request = new CreateEventNotificationScheduleRequest(
                LocalDateTime.of(2025, 1, 1, 10, 0),
                NotificationScheduleType.BEFORE_MINUTES,
                10);

        // when & then
        mockMvc.perform(post("/admin/api/v1/events/1/schedules")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(scheduleService).createSchedule(any(Long.class),
                any(CreateEventNotificationScheduleRequest.class));
    }

    @Test
    @DisplayName("이벤트에 대한 알림 스케줄 목록을 조회한다.")
    void 이벤트_알림_스케줄_목록을_조회한다() throws Exception {
        // given
        List<GetEventNotificationScheduleResponse> responses = List.of(
                new GetEventNotificationScheduleResponse(
                        1L,
                        LocalDateTime.of(2025, 1, 1, 10, 0),
                        NotificationScheduleType.BEFORE_MINUTES,
                        10,
                        false,
                        null));

        given(scheduleService.getSchedules(1L)).willReturn(responses);

        // when & then
        mockMvc.perform(get("/admin/api/v1/events/1/schedules"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("BEFORE_MINUTES"));
    }

    @Test
    @DisplayName("이벤트에 대한 단일 알림 스케줄을 조회한다.")
    void 이벤트_단일_알림_스케줄을_조회한다() throws Exception {
        // given
        GetEventNotificationScheduleResponse response = new GetEventNotificationScheduleResponse(
                1L,
                LocalDateTime.of(2025, 1, 1, 10, 0),
                NotificationScheduleType.BEFORE_MINUTES,
                10,
                false,
                null);

        given(scheduleService.getSchedule(1L, 1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/api/v1/events/1/schedules/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("BEFORE_MINUTES"));
    }

    @Test
    @DisplayName("이벤트에 대한 알림 스케줄을 수정한다.")
    void 이벤트_알림_스케줄을_수정한다() throws Exception {
        // given
        UpdateEventNotificationScheduleRequest request = new UpdateEventNotificationScheduleRequest(
                LocalDateTime.of(2025, 1, 1, 11, 0),
                NotificationScheduleType.AT_START,
                null);

        // when & then
        mockMvc.perform(patch("/admin/api/v1/events/1/schedules/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(scheduleService).updateSchedule(any(Long.class), any(Long.class),
                any(UpdateEventNotificationScheduleRequest.class));
    }

    @Test
    @DisplayName("이벤트에 대한 알림 스케줄을 삭제한다.")
    void 이벤트_알림_스케줄을_삭제한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/api/v1/events/1/schedules/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(scheduleService).deleteSchedule(any(Long.class), any(Long.class));
    }
}
