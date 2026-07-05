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
import me.bombom.api.v1.event.EventStatus;
import me.bombom.api.v1.event.dto.GetEventDetailResponse;
import me.bombom.api.v1.event.dto.GetEventResponse;
import me.bombom.api.v1.event.dto.GetEventsRequest;
import me.bombom.api.v1.event.dto.UpdateEventRequest;
import me.bombom.api.v1.event.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = EventController.class)
class EventControllerTest extends ControllerTestSupport {

    @MockitoBean
    private EventService eventService;

    @Test
    @DisplayName("이벤트를 생성한다.")
    void 이벤트를_생성한다() throws Exception {
        // given
        String requestBody = """
                {
                    "name": "이벤트 이름",
                    "startTime": "2025-01-01T10:00:00",
                    "status": "SCHEDULED"
                }
        """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        verify(eventService).createEvent(any(me.bombom.api.v1.event.dto.CreateEventRequest.class));
    }

    @Test
    @DisplayName("이벤트 목록을 조회한다.")
    void 이벤트_목록을_조회한다() throws Exception {
        // given
        GetEventResponse response = new GetEventResponse(
                1L,
                "이벤트 이름",
                LocalDateTime.now(),
                EventStatus.SCHEDULED);
        PageImpl<GetEventResponse> result = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 10),
                1);

        given(eventService.getEvents(any(GetEventsRequest.class), any(Pageable.class)))
                .willReturn(result);

        // when & then
        mockMvc.perform(get("/admin/api/v1/events")
                        .param("keyword", "이벤트")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("이벤트 이름"));
    }

    @Test
    @DisplayName("이벤트 상세를 조회한다.")
    void 이벤트_상세를_조회한다() throws Exception {
        // given
        GetEventDetailResponse response = new GetEventDetailResponse(
                1L,
                "이벤트 이름",
                LocalDateTime.now(),
                EventStatus.SCHEDULED);

        given(eventService.getEvent(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/api/v1/events/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("이벤트 이름"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("이벤트를 수정한다.")
    void 이벤트를_수정한다() throws Exception {
        // given
        UpdateEventRequest request = new UpdateEventRequest(
                "수정된 이벤트",
                LocalDateTime.now().plusDays(1),
                EventStatus.IN_PROGRESS);

        // when & then
        mockMvc.perform(patch("/admin/api/v1/events/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(eventService).updateEvent(any(Long.class), any(UpdateEventRequest.class));
    }

    @Test
    @DisplayName("이벤트를 삭제한다.")
    void 이벤트를_삭제한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/api/v1/events/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(any(Long.class));
    }
}
