package me.bombom.api.v1.dashboard.repository;

public interface DashboardMemberCounts {

    long getTotalMembers();

    long getDailyJoinedMembers();

    long getWeeklyJoinedMembers();

    long getMonthlyJoinedMembers();

    long getYearlyJoinedMembers();
}
