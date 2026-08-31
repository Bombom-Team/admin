package me.bombom.api.v1.dashboard.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DashboardCacheConfig {

    @Bean
    public Ticker dashboardCacheTicker() {
        return Ticker.systemTicker();
    }

    @Bean
    public Cache<DashboardCacheKey, DashboardStatsResponse> dashboardStatsCache(Ticker dashboardCacheTicker) {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(2)
                .ticker(dashboardCacheTicker)
                .build();
    }
}
