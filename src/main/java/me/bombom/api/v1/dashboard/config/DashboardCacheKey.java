package me.bombom.api.v1.dashboard.config;

import java.time.LocalDate;

public record DashboardCacheKey(
        String dataSource,
        LocalDate date
) {

}
