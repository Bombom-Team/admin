package me.bombom.api.v1.dashboard.dto;

public record DashboardStatsResponse(
        long totalMembers,
        long totalNotices,
        long newMembersThisMonth
) {
}
