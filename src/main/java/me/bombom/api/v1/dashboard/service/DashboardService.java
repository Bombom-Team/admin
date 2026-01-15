package me.bombom.api.v1.dashboard.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;

    public DashboardStatsResponse getStats() {
        long totalMembers = memberRepository.count();
        long totalNotices = noticeRepository.count();
        long newMembersThisMonth = memberRepository.countNewMembersThisMonth();

        return new DashboardStatsResponse(
                totalMembers,
                totalNotices,
                newMembersThisMonth
        );
    }
}
