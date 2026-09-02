package me.bombom.api.v1.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.config.datasource.DataSourceContextHolder;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.dashboard.config.DashboardCacheConfig;
import me.bombom.api.v1.dashboard.config.DashboardCacheKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@Import({DashboardService.class, QuerydslConfig.class, DashboardCacheConfig.class,
        DashboardAggregationTest.TestTimeConfig.class})
class DashboardAggregationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Cache<DashboardCacheKey, DashboardStatsResponse> dashboardStatsCache;

    @Autowired
    private MutableClock clock;

    @Autowired
    private AtomicLong tickerNanos;

    @BeforeEach
    void 캐시를_초기화한다() {
        dashboardStatsCache.invalidateAll();
        DataSourceContextHolder.clear();
        clock.instant = Instant.parse("2026-09-01T16:00:00Z");
        tickerNanos.set(0L);
    }

    @AfterEach
    void 데이터소스_컨텍스트를_해제한다() {
        DataSourceContextHolder.clear();
    }

    @Test
    void 운영과_개발_DB_조회는_각자의_캐시를_사용한다() {
        // given
        createMember(1L, 1L, "2026-09-02T00:00:00");
        DataSourceContextHolder.setContext(DataSourceContextHolder.DEV);
        DashboardStatsResponse dev = dashboardService.getStats();
        createMember(2L, 1L, "2026-09-02T00:00:00");
        DataSourceContextHolder.setContext(DataSourceContextHolder.PROD);

        // when
        DashboardStatsResponse prod = dashboardService.getStats();
        DataSourceContextHolder.setContext(DataSourceContextHolder.DEV);
        DashboardStatsResponse devAgain = dashboardService.getStats();
        DataSourceContextHolder.clear();
        DashboardStatsResponse defaultDb = dashboardService.getStats();

        // then
        assertSoftly(softly -> {
            softly.assertThat(dev.totalMembers()).isEqualTo(1);
            softly.assertThat(prod.totalMembers()).isEqualTo(2);
            softly.assertThat(devAgain).isSameAs(dev);
            softly.assertThat(defaultDb).isSameAs(prod);
        });
    }

    @Test
    void 캐시_5분_만료_후에는_회원_변경을_다시_집계한다() {
        // given
        createMember(1L, 1L, "2026-09-02T00:00:00");
        dashboardService.getStats();
        createMember(2L, 4L, "2026-09-02T00:00:00");
        createMember(3L, 1L, "2026-09-02T00:00:00");
        tickerNanos.set(Duration.ofMinutes(5).toNanos());

        // when
        DashboardStatsResponse response = dashboardService.getStats();

        // then
        assertThat(response.totalMembers()).isEqualTo(2);
    }

    @Test
    void 자정이_지나면_캐시가_남아도_새_날짜로_집계한다() {
        // given
        clock.instant = Instant.parse("2026-09-02T14:59:59Z");
        createMember(1L, 1L, "2026-09-02T00:00:00");
        DashboardStatsResponse beforeMidnight = dashboardService.getStats();
        clock.instant = Instant.parse("2026-09-02T15:00:00Z");

        // when
        DashboardStatsResponse afterMidnight = dashboardService.getStats();

        // then
        assertSoftly(softly -> {
            softly.assertThat(beforeMidnight.dailyJoinedMembers()).isEqualTo(1);
            softly.assertThat(afterMidnight.dailyJoinedMembers()).isZero();
            softly.assertThat(afterMidnight.dailyJoinedTrend().getLast().date()).isEqualTo(LocalDate.of(2026, 9, 3));
        });
    }

    @Test
    void 같은날_연속_조회는_집계_결과를_재사용한다() {
        // given
        createMember(1L, 1L, "2026-09-02T00:00:00");
        DashboardStatsResponse first = dashboardService.getStats();
        createMember(2L, 1L, "2026-09-02T00:00:00");

        // when
        DashboardStatsResponse second = dashboardService.getStats();

        // then
        assertSoftly(softly -> {
            softly.assertThat(second.totalMembers()).isEqualTo(1);
            softly.assertThat(second.aggregatedAt()).isEqualTo(first.aggregatedAt());
        });
    }

    @Test
    void 서울_날짜의_기간_시작은_포함하고_다음날은_제외한다() {
        // given
        createMember(1L, 1L, "2025-12-31T23:59:59");
        createMember(2L, 1L, "2026-01-01T00:00:00");
        createMember(3L, 1L, "2026-08-03T23:59:59");
        createMember(4L, 1L, "2026-08-04T00:00:00");
        createMember(5L, 1L, "2026-08-26T23:59:59");
        createMember(6L, 2L, "2026-08-27T00:00:00");
        createMember(7L, 3L, "2026-08-31T23:59:59");
        createMember(8L, 1L, "2026-09-01T00:00:00");
        createMember(9L, 1L, "2026-09-02T00:00:00");
        createMember(10L, 1L, "2026-09-03T00:00:00");
        createMember(11L, 4L, "2026-09-02T00:00:00");

        // when
        DashboardStatsResponse response = dashboardService.getStats();

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.totalMembers()).isEqualTo(10);
            softly.assertThat(response.dailyJoinedMembers()).isEqualTo(1);
            softly.assertThat(response.weeklyJoinedMembers()).isEqualTo(4);
            softly.assertThat(response.monthlyJoinedMembers()).isEqualTo(2);
            softly.assertThat(response.yearlyJoinedMembers()).isEqualTo(8);
        });
    }

    @Test
    void 일별_가입_추이는_30일을_오름차순으로_채우고_테스트계정을_제외한다() {
        // given
        createMember(1L, 1L, "2026-08-03T23:59:59");
        createMember(2L, 2L, "2026-08-04T00:00:00");
        createMember(3L, 1L, "2026-09-02T00:00:00");
        createMember(4L, 3L, "2026-09-02T01:00:00");
        createMember(5L, 4L, "2026-09-02T01:00:00");
        createMember(6L, 1L, "2026-09-03T00:00:00");

        // when
        JsonNode response = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .valueToTree(dashboardService.getStats());

        // then
        JsonNode trend = response.path("dailyJoinedTrend");
        assertThat(trend.isArray()).isTrue();
        assertSoftly(softly -> {
            softly.assertThat(trend.size()).isEqualTo(30);
            softly.assertThat(trend.get(0).path("date").asText()).isEqualTo("2026-08-04");
            softly.assertThat(trend.get(0).path("count").asLong()).isEqualTo(1);
            softly.assertThat(trend.get(1).path("count").asLong()).isZero();
            softly.assertThat(trend.get(29).path("date").asText()).isEqualTo("2026-09-02");
            softly.assertThat(trend.get(29).path("count").asLong()).isEqualTo(2);
        });
    }

    private void createMember(long id, long roleId, String createdAt) {
        jdbcTemplate.update("""
                INSERT INTO member (id, provider, provider_id, email, nickname, gender, role_id, created_at, updated_at)
                VALUES (?, 'GOOGLE', ?, ?, ?, 'MALE', ?, ?, ?)
                """, id, "provider-" + id, "member-" + id + "@example.com", "member-" + id,
                roleId, LocalDateTime.parse(createdAt), LocalDateTime.parse(createdAt));
    }

    @TestConfiguration
    static class TestTimeConfig {

        @Bean
        MutableClock clock() {
            return new MutableClock();
        }

        @Bean
        AtomicLong tickerNanos() {
            return new AtomicLong();
        }

        @Bean
        @Primary
        Ticker testTicker(AtomicLong tickerNanos) {
            return tickerNanos::get;
        }
    }

    static class MutableClock extends Clock {

        private Instant instant = Instant.parse("2026-09-01T16:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneId.of("Asia/Seoul");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
