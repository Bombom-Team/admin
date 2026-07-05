package me.bombom.api.v1.challenge.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChallengeDailyGuideRepository extends JpaRepository<ChallengeDailyGuide, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.GetDailyGuideResponse(
            g.id, g.challengeId, g.dayIndex, g.type, g.imageUrl, g.notice, g.commentEnabled
        )
        FROM ChallengeDailyGuide g
        WHERE g.challengeId = :challengeId
        ORDER BY g.dayIndex ASC
    """)
    List<GetDailyGuideResponse> findAllByChallengeIdAsResponse(Long challengeId);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.GetDailyGuideResponse(
            g.id, g.challengeId, g.dayIndex, g.type, g.imageUrl, g.notice, g.commentEnabled
        )
        FROM ChallengeDailyGuide g
        WHERE g.id = :guideId AND g.challengeId = :challengeId
    """)
    Optional<GetDailyGuideResponse> findByIdAndChallengeIdAsResponse(Long guideId, Long challengeId);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.GetDailyGuideResponse(
            g.id, g.challengeId, g.dayIndex, g.type, g.imageUrl, g.notice, g.commentEnabled
        )
        FROM ChallengeDailyGuide g
        WHERE g.challengeId = :challengeId AND g.dayIndex = :dayIndex
    """)
    Optional<GetDailyGuideResponse> findByChallengeIdAndDayIndexAsResponse(Long challengeId, int dayIndex);

    boolean existsByChallengeIdAndDayIndex(Long challengeId, int dayIndex);

    boolean existsByChallengeIdAndDayIndexAndIdNot(Long challengeId, int dayIndex, Long id);
}
