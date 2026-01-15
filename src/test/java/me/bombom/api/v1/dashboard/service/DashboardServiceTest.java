package me.bombom.api.v1.dashboard.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;

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

    @Test
    @DisplayName("대시보드 통계를 조회한다.")
    void getStats() {
        // given
        saveMember("kakao", "12345", "test1@example.com", "테스트1", Gender.MALE);
        saveMember("kakao", "67890", "test2@example.com", "테스트2", Gender.FEMALE);

        saveNotice("공지사항 1", "내용 1", NoticeCategory.NOTICE);
        saveNotice("공지사항 2", "내용 2", NoticeCategory.EVENT);

        // when
        DashboardStatsResponse response = dashboardService.getStats();

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.totalMembers()).isEqualTo(2);
            softly.assertThat(response.totalNotices()).isEqualTo(2);
            softly.assertThat(response.newMembersThisMonth()).isEqualTo(2);
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
            softly.assertThat(response.newMembersThisMonth()).isZero();
        });
    }

    private void saveMember(String provider, String providerId, String email, String nickname, Gender gender) {
        memberRepository.save(Member.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .gender(gender)
                .roleId(1L)
                .build());
    }

    private void saveNotice(String title, String content, NoticeCategory category) {
        noticeRepository.save(Notice.builder()
                .title(title)
                .content(content)
                .noticeCategory(category)
                .build());
    }
}
