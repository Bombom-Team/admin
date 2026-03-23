package me.bombom.api.v1.challenge.repository;

import java.util.List;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeDailyGuideRepository extends JpaRepository<ChallengeDailyGuide, Long> {

    List<ChallengeDailyGuide> findByChallengeIdOrderByDayIndexAsc(Long challengeId);
}
