package me.bombom.api.v1.holiday.controller;

import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.holiday.dto.CreateHolidayRequest;
import me.bombom.api.v1.holiday.dto.CreateHolidaysRequest;
import me.bombom.api.v1.holiday.dto.GetHolidayResponse;
import me.bombom.api.v1.holiday.dto.GetHolidaysRequest;
import me.bombom.api.v1.holiday.dto.UpdateHolidayRequest;
import me.bombom.api.v1.holiday.service.HolidayService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

@WebMvcTest(controllers = HolidayController.class)
class HolidayControllerTest extends ControllerTestSupport {

    @MockitoBean
    private HolidayService holidayService;

    @Test
    void 공휴일_목록_조회() throws Exception {
        // given
        given(holidayService.getHolidays(any(GetHolidaysRequest.class)))
                .willReturn(List.of(new GetHolidayResponse(1L, LocalDate.of(2026, 1, 1), "신정")));

        // when & then
        mockMvc.perform(get("/admin/api/v1/holidays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].date").value("2026-01-01"))
                .andExpect(jsonPath("$[0].name").value("신정"));
    }

    @Test
    void 공휴일_목록_조회_year_쿼리스트링_바인딩() throws Exception {
        // given
        ArgumentCaptor<GetHolidaysRequest> captor = ArgumentCaptor.forClass(GetHolidaysRequest.class);
        given(holidayService.getHolidays(any(GetHolidaysRequest.class))).willReturn(List.of());

        // when
        mockMvc.perform(get("/admin/api/v1/holidays").param("year", "2026"))
                .andExpect(status().isOk());

        // then
        verify(holidayService).getHolidays(captor.capture());
        assertThat(captor.getValue().year()).isEqualTo(2026);
    }

    @Test
    void 공휴일_목록_조회_year가_숫자가_아니면_400() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/api/v1/holidays").param("year", "이천이십육"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 공휴일_등록() throws Exception {
        // given
        CreateHolidayRequest request = new CreateHolidayRequest(LocalDate.of(2026, 1, 1), "신정");

        // when & then
        mockMvc.perform(post("/admin/api/v1/holidays")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(holidayService).createHoliday(any(CreateHolidayRequest.class));
    }

    @Test
    void 공휴일_등록_date가_없으면_400() throws Exception {
        // given
        String requestBody = """
                {
                    "name": "신정"
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/holidays")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 공휴일_등록_name이_공백이면_400() throws Exception {
        // given
        String requestBody = """
                {
                    "date": "2026-01-01",
                    "name": " "
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/holidays")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 공휴일_일괄_등록() throws Exception {
        // given
        CreateHolidaysRequest request = new CreateHolidaysRequest(List.of(
                new CreateHolidayRequest(LocalDate.of(2026, 1, 1), "신정"),
                new CreateHolidayRequest(LocalDate.of(2026, 3, 1), "삼일절")));

        // when & then
        mockMvc.perform(post("/admin/api/v1/holidays/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(holidayService).createHolidays(any(CreateHolidaysRequest.class));
    }

    @Test
    void 공휴일_일괄_등록_목록이_비어있으면_400() throws Exception {
        // given
        String requestBody = """
                {
                    "holidays": []
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/holidays/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 공휴일_일괄_등록_항목에_date가_없으면_400() throws Exception {
        // given
        String requestBody = """
                {
                    "holidays": [
                        { "name": "신정" }
                    ]
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/holidays/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 공휴일_수정() throws Exception {
        // given
        UpdateHolidayRequest request = new UpdateHolidayRequest(LocalDate.of(2026, 3, 2), "삼일절 대체공휴일");

        // when & then
        mockMvc.perform(patch("/admin/api/v1/holidays/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(holidayService).updateHoliday(any(Long.class), any(UpdateHolidayRequest.class));
    }

    @Test
    void 공휴일_삭제() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/api/v1/holidays/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(holidayService).deleteHoliday(1L);
    }
}
