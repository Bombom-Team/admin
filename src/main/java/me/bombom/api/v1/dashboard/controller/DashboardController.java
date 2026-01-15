package me.bombom.api.v1.dashboard.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/dashboard")
public class DashboardController implements DashboardControllerApi {

    private final DashboardService dashboardService;

    @Override
    @GetMapping("/stats")
    public DashboardStatsResponse getStats() {
        return dashboardService.getStats();
    }
}
