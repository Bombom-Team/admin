package me.bombom.api.v1.dashboard.dto;

import java.time.Instant;
import java.util.List;

public record DashboardStatsResponse(
        long totalMembers,
        long totalNotices,
        long dailyJoinedMembers,
        long weeklyJoinedMembers,
        long monthlyJoinedMembers,
        long yearlyJoinedMembers,
        long withdrawnMembersThisMonth,
        long todayActiveMembers,
        List<DailyJoinedMembersResponse> dailyJoinedTrend,
        Instant aggregatedAt
) {

    public static DashboardStatsResponse of(
            long totalMembers,
            long totalNotices,
            long dailyJoinedMembers,
            long weeklyJoinedMembers,
            long monthlyJoinedMembers,
            long yearlyJoinedMembers,
            long withdrawnMembersThisMonth,
            long todayActiveMembers,
            List<DailyJoinedMembersResponse> dailyJoinedTrend,
            Instant aggregatedAt
    ) {
        return new DashboardStatsResponse(
                totalMembers,
                totalNotices,
                dailyJoinedMembers,
                weeklyJoinedMembers,
                monthlyJoinedMembers,
                yearlyJoinedMembers,
                withdrawnMembersThisMonth,
                todayActiveMembers,
                List.copyOf(dailyJoinedTrend),
                aggregatedAt
        );
    }
}
