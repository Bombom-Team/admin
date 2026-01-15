package me.bombom.api.v1.withdraw.repository;

import java.time.LocalDate;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawnMemberRepository extends JpaRepository<WithdrawnMember, Long> {

    /**
     * 이번 달 탈퇴한 회원 수 조회
     */
    @Query("""
        SELECT COUNT(w)
        FROM WithdrawnMember w
        WHERE w.deletedDate >= :startOfMonth
    """)
    long countDeletedMembersThisMonth(@Param("startOfMonth") LocalDate startOfMonth);
}
