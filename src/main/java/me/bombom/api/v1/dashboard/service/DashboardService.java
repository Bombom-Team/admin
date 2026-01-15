package me.bombom.api.v1.dashboard.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.api.v1.session.repository.SpringSessionRepository;
import me.bombom.api.v1.withdraw.repository.WithdrawnMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final WithdrawnMemberRepository withdrawnMemberRepository;
    private final SpringSessionRepository springSessionRepository;

    public DashboardStatsResponse getStats() {
        long totalMembers = memberRepository.count();
        long totalNotices = noticeRepository.count();
        long newMembersThisMonth = memberRepository.countNewMembersThisMonth();
        long todayJoinedMembers = memberRepository.countTodayJoinedMembers();

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        long withdrawnMembersThisMonth = withdrawnMemberRepository.countDeletedMembersThisMonth(startOfMonth);

        // 오늘 활동한 유저 수 (Spring Session 기반)
        long todayStartMillis = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        long nowMillis = Instant.now().toEpochMilli();
        long todayActiveUsers = springSessionRepository.countTodayActiveUsers(todayStartMillis, nowMillis);

        return DashboardStatsResponse.of(
                totalMembers,
                totalNotices,
                newMembersThisMonth,
                todayJoinedMembers,
                todayActiveUsers,
                withdrawnMembersThisMonth);
    }
}
