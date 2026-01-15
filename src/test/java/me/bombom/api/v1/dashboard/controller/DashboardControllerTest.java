package me.bombom.api.v1.dashboard.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.dashboard.service.DashboardService;
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
                1234L, 42L, 89L, 10L, 5L, 2L);
        given(dashboardService.getStats()).willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/api/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(1234))
                .andExpect(jsonPath("$.totalNotices").value(42))
                .andExpect(jsonPath("$.newMembersThisMonth").value(89))
                .andExpect(jsonPath("$.todayJoinedMembers").value(10))
                .andExpect(jsonPath("$.todayActiveUsers").value(5))
                .andExpect(jsonPath("$.withdrawnMembersThisMonth").value(2));
    }
}
