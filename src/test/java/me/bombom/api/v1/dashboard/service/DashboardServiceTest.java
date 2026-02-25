package me.bombom.api.v1.dashboard.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.member.fixture.MemberFixture;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.fixture.NoticeFixture;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.api.v1.session.repository.SpringSessionRepository;
import me.bombom.api.v1.withdraw.fixture.WithdrawnMemberFixture;
import me.bombom.api.v1.withdraw.repository.WithdrawnMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@EnableJpaAuditing
@Import({ DashboardService.class, QuerydslConfig.class })
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private WithdrawnMemberRepository withdrawnMemberRepository;

    @MockitoBean
    private SpringSessionRepository springSessionRepository;

    @Test
    @DisplayName("대시보드 통계를 조회한다.")
    void getStats() {
        // given
        memberRepository.save(MemberFixture.createMember("테스트1"));
        memberRepository.save(MemberFixture.createMember("테스트2"));

        noticeRepository.save(NoticeFixture.createNotice("공지사항 1", "내용 1", NoticeCategory.NOTICE));
        noticeRepository.save(NoticeFixture.createNotice("공지사항 2", "내용 2", NoticeCategory.EVENT));

        withdrawnMemberRepository
                .save(WithdrawnMemberFixture.createWithdrawnMember(1L, "withdrawn@example.com", LocalDate.now()));

        given(springSessionRepository.countTodayActiveUsers(anyLong(), anyLong())).willReturn(5L);

        // when
        DashboardStatsResponse response = dashboardService.getStats();

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.totalMembers()).isEqualTo(2);
            softly.assertThat(response.totalNotices()).isEqualTo(2);
            softly.assertThat(response.dailyJoinedMembers()).isEqualTo(2);
            softly.assertThat(response.weeklyJoinedMembers()).isEqualTo(2);
            softly.assertThat(response.monthlyJoinedMembers()).isEqualTo(2);
            softly.assertThat(response.yearlyJoinedMembers()).isEqualTo(2);
            softly.assertThat(response.withdrawnMembersThisMonth()).isEqualTo(1);
            softly.assertThat(response.todayActiveMembers()).isEqualTo(5);
        });
    }

    @Test
    @DisplayName("데이터가 없을 때 통계를 조회하면 모든 값이 0이다.")
    void getStats_empty() {
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
        });
    }

}
