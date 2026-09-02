package me.bombom.api.v1.dashboard.service;

import com.github.benmanes.caffeine.cache.Cache;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.config.datasource.DataSourceContextHolder;
import me.bombom.api.v1.dashboard.config.DashboardCacheKey;
import me.bombom.api.v1.dashboard.dto.DailyJoinedMembersResponse;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.dashboard.repository.DashboardMemberCounts;
import me.bombom.api.v1.dashboard.repository.DashboardMemberRepository;
import me.bombom.api.v1.dashboard.repository.DailyJoinedMembersCount;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.api.v1.session.repository.SpringSessionRepository;
import me.bombom.api.v1.withdraw.repository.WithdrawnMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final long TEST_ROLE_ID = 4L;

    private final DashboardMemberRepository dashboardMemberRepository;
    private final NoticeRepository noticeRepository;
    private final WithdrawnMemberRepository withdrawnMemberRepository;
    private final SpringSessionRepository springSessionRepository;
    private final Clock clock;
    private final Cache<DashboardCacheKey, DashboardStatsResponse> dashboardStatsCache;

    public DashboardStatsResponse getStats() {
        LocalDate today = LocalDate.now(clock);
        String dataSource = DataSourceContextHolder.DEV.equals(DataSourceContextHolder.getContext())
                ? DataSourceContextHolder.DEV : DataSourceContextHolder.PROD;
        DashboardCacheKey cacheKey = new DashboardCacheKey(
                dataSource,
                today
        );
        return dashboardStatsCache.get(cacheKey, key -> loadStats(key.date()));
    }

    private DashboardStatsResponse loadStats(LocalDate today) {
        DashboardMemberCounts counts = dashboardMemberRepository.countMembers(
                TEST_ROLE_ID,
                today.atStartOfDay(),
                today.minusDays(6).atStartOfDay(),
                today.withDayOfMonth(1).atStartOfDay(),
                today.withDayOfYear(1).atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );
        long totalNotices = noticeRepository.count();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        long withdrawnMembersThisMonth = withdrawnMemberRepository.countDeletedMembersThisMonth(startOfMonth);

        long todayActiveMembers = countTodayActiveMembers(today);

        return DashboardStatsResponse.of(
                counts.getTotalMembers(),
                totalNotices,
                counts.getDailyJoinedMembers(),
                counts.getWeeklyJoinedMembers(),
                counts.getMonthlyJoinedMembers(),
                counts.getYearlyJoinedMembers(),
                withdrawnMembersThisMonth,
                todayActiveMembers,
                getDailyJoinedTrend(today),
                clock.instant()
        );
    }

    private List<DailyJoinedMembersResponse> getDailyJoinedTrend(LocalDate today) {
        LocalDate firstDay = today.minusDays(29);
        Map<LocalDate, Long> counts = dashboardMemberRepository.countDailyJoinedMembers(
                        TEST_ROLE_ID, firstDay.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .stream()
                .collect(Collectors.toMap(DailyJoinedMembersCount::getDate, DailyJoinedMembersCount::getCount));

        return firstDay.datesUntil(today.plusDays(1))
                .map(date -> DailyJoinedMembersResponse.of(date, counts.getOrDefault(date, 0L)))
                .toList();
    }

    private long countTodayActiveMembers(LocalDate today) {
        long startOfTodayMillis = today
                .atStartOfDay(clock.getZone())
                .toInstant()
                .toEpochMilli();

        return springSessionRepository.countTodayActiveUsers(
                startOfTodayMillis,
                clock.millis(),
                TEST_ROLE_ID
        );
    }
}
