package me.bombom.api.v1.challenge.repository;

import java.util.List;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeTeamRepository extends JpaRepository<ChallengeTeam, Long> {

    List<ChallengeTeam> findByChallengeId(Long challengeId);
}
