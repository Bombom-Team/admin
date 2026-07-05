package me.bombom.api.v1.dev.service;

import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScenarioFactoryService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeTodoRepository challengeTodoRepository;
    private final MemberRepository memberRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;

    public void createStoppedChallengeScenario() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now().minusDays(1);

        Challenge stoppedChallenge = Challenge.builder()
                .name("종료된 챌린지 시나리오")
                .generation(1)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        challengeRepository.save(stoppedChallenge);

        ChallengeTodo todo1 = ChallengeTodo.builder()
                .challengeId(stoppedChallenge.getId())
                .todoType(ChallengeTodoType.READ)
                .build();

        ChallengeTodo todo2 = ChallengeTodo.builder()
                .challengeId(stoppedChallenge.getId())
                .todoType(ChallengeTodoType.COMMENT)
                .build();

        challengeTodoRepository.saveAll(List.of(todo1, todo2));
        log.info("Created stopped challenge: {}", stoppedChallenge.getId());
    }
}
