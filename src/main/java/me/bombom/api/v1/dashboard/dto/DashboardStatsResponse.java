package me.bombom.api.v1.dashboard.dto;

public record DashboardStatsResponse(
        long totalMembers,
        long totalNotices,
        long newMembersThisMonth,
        long todayJoinedMembers,
        long withdrawnMembersThisMonth
) {

    public static DashboardStatsResponse of(
            long totalMembers,
            long totalNotices,
            long newMembersThisMonth,
            long todayJoinedMembers,
            long withdrawnMembersThisMonth
    ) {
        return new DashboardStatsResponse(
                totalMembers,
                totalNotices,
                newMembersThisMonth,
                todayJoinedMembers,
                withdrawnMembersThisMonth
        );
    }
}
