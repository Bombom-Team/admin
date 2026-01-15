package me.bombom.api.v1.dashboard.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.notice.repository.NoticeRepository;
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

    public DashboardStatsResponse getStats() {
        long totalMembers = memberRepository.count();
        long totalNotices = noticeRepository.count();

        long dailyJoinedMembers = memberRepository.countDailyJoinedMembers();
        long weeklyJoinedMembers = memberRepository.countWeeklyJoinedMembers();
        long monthlyJoinedMembers = memberRepository.countMonthlyJoinedMembers();
        long yearlyJoinedMembers = memberRepository.countYearlyJoinedMembers();

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        long withdrawnMembersThisMonth = withdrawnMemberRepository.countDeletedMembersThisMonth(startOfMonth);

        return DashboardStatsResponse.of(
                totalMembers,
                totalNotices,
                dailyJoinedMembers,
                weeklyJoinedMembers,
                monthlyJoinedMembers,
                yearlyJoinedMembers,
                withdrawnMembersThisMonth);
    }
}
