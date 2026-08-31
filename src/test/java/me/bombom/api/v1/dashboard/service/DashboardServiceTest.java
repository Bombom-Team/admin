package me.bombom.api.v1.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.github.benmanes.caffeine.cache.Cache;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.dashboard.config.DashboardCacheConfig;
import me.bombom.api.v1.dashboard.config.DashboardCacheKey;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import me.bombom.api.v1.withdraw.repository.WithdrawnMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@Import({DashboardService.class, QuerydslConfig.class, DashboardCacheConfig.class,
        DashboardServiceTest.TestTimeConfig.class})
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private WithdrawnMemberRepository withdrawnMemberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    @Autowired
    private Cache<DashboardCacheKey, DashboardStatsResponse> dashboardStatsCache;

    @BeforeEach
    void 캐시를_초기화한다() {
        dashboardStatsCache.invalidateAll();
    }

    @Test
    void 회원수와_가입수에서_권한_4만_제외한다() {
        // given
        createMember("일반", 1L);
        createMember("관리자", 2L);
        createMember("기타", 3L);
        createMember("테스트", 4L);

        // when
        DashboardStatsResponse response = dashboardService.getStats();

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.totalMembers()).isEqualTo(3);
            softly.assertThat(response.dailyJoinedMembers()).isEqualTo(3);
            softly.assertThat(response.weeklyJoinedMembers()).isEqualTo(3);
            softly.assertThat(response.monthlyJoinedMembers()).isEqualTo(3);
            softly.assertThat(response.yearlyJoinedMembers()).isEqualTo(3);
        });
    }

    @Test
    void 공지와_탈퇴_집계를_유지하고_유효한_오늘_세션만_중복제거한다() {
        // given
        createMember("일반", 1L);
        noticeRepository.save(Notice.builder()
                .title("공지")
                .content("내용")
                .noticeCategory(NoticeCategory.NOTICE)
                .build());
        LocalDate today = LocalDate.now(clock);
        withdrawnMemberRepository.save(WithdrawnMember.builder()
                .memberId(99L)
                .email("withdrawn@example.com")
                .gender(Gender.MALE)
                .joinedDate(today.minusMonths(1))
                .deletedDate(today)
                .expireDate(today.plusDays(30))
                .build());
        long now = clock.millis();
        long todayStart = today.atStartOfDay(clock.getZone())
                .toInstant()
                .toEpochMilli();
        createSession("one", "일반", todayStart, now + 60_000);
        createSession("duplicate", "일반", now, now + 60_000);
        createSession("expired", "만료", now, now);
        createSession("yesterday", "어제", todayStart - 1, now + 60_000);

        // when
        DashboardStatsResponse response = dashboardService.getStats();

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.totalNotices()).isEqualTo(1);
            softly.assertThat(response.withdrawnMembersThisMonth()).isEqualTo(1);
            softly.assertThat(response.todayActiveMembers()).isEqualTo(1);
        });
    }

    @Test
    void 데이터가_없으면_집계와_30일_추이는_0이다() {
        // when
        DashboardStatsResponse response = dashboardService.getStats();

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.totalMembers()).isZero();
            softly.assertThat(response.totalNotices()).isZero();
            softly.assertThat(response.dailyJoinedMembers()).isZero();
            softly.assertThat(response.weeklyJoinedMembers()).isZero();
            softly.assertThat(response.monthlyJoinedMembers()).isZero();
            softly.assertThat(response.yearlyJoinedMembers()).isZero();
            softly.assertThat(response.withdrawnMembersThisMonth()).isZero();
            softly.assertThat(response.todayActiveMembers()).isZero();
            softly.assertThat(response.dailyJoinedTrend()).hasSize(30);
            softly.assertThat(response.dailyJoinedTrend()).allSatisfy(point ->
                    assertThat(point.count()).isZero());
        });
    }

    private void createMember(String nickname, Long roleId) {
        LocalDateTime createdAt = LocalDateTime.now(clock);
        jdbcTemplate.update("""
                INSERT INTO member (provider, provider_id, email, nickname, gender, role_id, created_at, updated_at)
                VALUES ('GOOGLE', ?, ?, ?, 'MALE', ?, ?, ?)
                """, nickname, nickname + "@example.com", nickname, roleId, createdAt, createdAt);
    }

    private void createSession(
            String id,
            String principal,
            long accessedAt,
            long expiresAt
    ) {
        jdbcTemplate.update("""
                INSERT INTO spring_session (primary_id, session_id, creation_time, last_access_time,
                                            max_inactive_interval, expiry_time, principal_name)
                VALUES (?, ?, ?, ?, 60, ?, ?)
                """, id, id, accessedAt, accessedAt, expiresAt, principal);
    }

    @TestConfiguration
    static class TestTimeConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-09-01T16:00:00Z"), ZoneId.of("Asia/Seoul"));
        }

    }
}
