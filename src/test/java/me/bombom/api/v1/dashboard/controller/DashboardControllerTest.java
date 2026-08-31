package me.bombom.api.v1.dashboard.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.dashboard.dto.DailyJoinedMembersResponse;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.dashboard.service.DashboardService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest extends ControllerTestSupport {

    @MockitoBean
    protected DashboardService dashboardService;

    @Test
    @DisplayName("대시보드 통계를 조회한다.")
    void getStats() throws Exception {
        // given
        DashboardStatsResponse response = new DashboardStatsResponse(
                1234L, 42L, 10L, 89L, 234L, 567L, 2L, 5L,
                List.of(DailyJoinedMembersResponse.of(LocalDate.of(2026, 9, 2), 10)),
                Instant.parse("2026-09-01T16:00:00Z")
        );
        given(dashboardService.getStats()).willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/api/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(1234))
                .andExpect(jsonPath("$.totalNotices").value(42))
                .andExpect(jsonPath("$.dailyJoinedMembers").value(10))
                .andExpect(jsonPath("$.weeklyJoinedMembers").value(89))
                .andExpect(jsonPath("$.monthlyJoinedMembers").value(234))
                .andExpect(jsonPath("$.yearlyJoinedMembers").value(567))
                .andExpect(jsonPath("$.withdrawnMembersThisMonth").value(2))
                .andExpect(jsonPath("$.todayActiveMembers").value(5))
                .andExpect(jsonPath("$.dailyJoinedTrend[0].date").value("2026-09-02"))
                .andExpect(jsonPath("$.dailyJoinedTrend[0].count").value(10))
                .andExpect(jsonPath("$.aggregatedAt").value("2026-09-01T16:00:00Z"));
    }
}
