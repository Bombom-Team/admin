package me.bombom.api.v1.dashboard.repository;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DashboardMemberRepository extends JpaRepository<Member, Long> {

    @Query(value = """
            SELECT CAST(created_at AS DATE) AS date, COUNT(*) AS count
            FROM member
            WHERE role_id <> :excludedRoleId
              AND created_at >= :startDate
              AND created_at < :endDate
            GROUP BY CAST(created_at AS DATE)
            ORDER BY CAST(created_at AS DATE)
            """, nativeQuery = true)
    List<DailyJoinedMembersCount> countDailyJoinedMembers(
            @Param("excludedRoleId") long excludedRoleId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
            SELECT COUNT(*) AS totalMembers,
                   COALESCE(SUM(CASE WHEN created_at >= :todayStart AND created_at < :tomorrowStart
                       THEN 1 ELSE 0 END), 0) AS dailyJoinedMembers,
                   COALESCE(SUM(CASE WHEN created_at >= :weekStart AND created_at < :tomorrowStart
                       THEN 1 ELSE 0 END), 0) AS weeklyJoinedMembers,
                   COALESCE(SUM(CASE WHEN created_at >= :monthStart AND created_at < :tomorrowStart
                       THEN 1 ELSE 0 END), 0) AS monthlyJoinedMembers,
                   COALESCE(SUM(CASE WHEN created_at >= :yearStart AND created_at < :tomorrowStart
                       THEN 1 ELSE 0 END), 0) AS yearlyJoinedMembers
            FROM member
            WHERE role_id <> :excludedRoleId
            """, nativeQuery = true)
    DashboardMemberCounts countMembers(
            @Param("excludedRoleId") long excludedRoleId,
            @Param("todayStart") LocalDateTime todayStart,
            @Param("weekStart") LocalDateTime weekStart,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("yearStart") LocalDateTime yearStart,
            @Param("tomorrowStart") LocalDateTime tomorrowStart
    );
}
