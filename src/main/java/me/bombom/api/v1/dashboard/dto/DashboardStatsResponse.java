package me.bombom.api.v1.dashboard.dto;

public record DashboardStatsResponse(
        long totalMembers,
        long totalNotices,
        long dailyJoinedMembers,
        long weeklyJoinedMembers,
        long monthlyJoinedMembers,
        long yearlyJoinedMembers,
        long withdrawnMembersThisMonth) {

    public static DashboardStatsResponse of(
            long totalMembers,
            long totalNotices,
            long dailyJoinedMembers,
            long weeklyJoinedMembers,
            long monthlyJoinedMembers,
            long yearlyJoinedMembers,
            long withdrawnMembersThisMonth) {
        return new DashboardStatsResponse(
                totalMembers,
                totalNotices,
                dailyJoinedMembers,
                weeklyJoinedMembers,
                monthlyJoinedMembers,
                yearlyJoinedMembers,
                withdrawnMembersThisMonth);
    }
}
