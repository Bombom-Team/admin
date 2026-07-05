package me.bombom.api.v1.challenge.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long>, CustomChallengeParticipantRepository {

    List<ChallengeParticipant> findAllByChallengeId(Long challengeId);

    boolean existsByChallengeId(Long challengeId);

    Optional<ChallengeParticipant> findByChallengeIdAndMemberId(Long challengeId, Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE ChallengeParticipant p
        SET p.challengeTeamId = NULL
        WHERE p.challengeTeamId = :challengeTeamId
    """)
    void updateChallengeTeamIdToNull(@Param("challengeTeamId") Long challengeTeamId);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE ChallengeParticipant p
        SET p.shield = (p.shield + :incrementCount)
        WHERE p.challengeId = :challengeId AND p.isSurvived = true
    """)
    void incrementShieldByChallengeId(
            @Param("challengeId") Long challengeId,
            @Param("incrementCount") int incrementCount
    );
}
