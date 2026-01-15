package me.bombom.api.v1.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SpringSessionEntity, String> {

    @Query(value = """
        SELECT COUNT(DISTINCT s.PRINCIPAL_NAME)
        FROM SPRING_SESSION s
        WHERE s.LAST_ACCESS_TIME >= :todayStartMillis
          AND s.EXPIRY_TIME > :nowMillis
    """, nativeQuery = true)
    long countTodayActiveUsers(
            @Param("todayStartMillis") long todayStartMillis,
            @Param("nowMillis") long nowMillis
    );
}
