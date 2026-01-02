package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@EnableJpaAuditing
@Import({ ChallengeService.class, QuerydslConfig.class })
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class ChallengeServiceTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Test
    void 참여자가_15명일_때_1개의_팀이_생성된다() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 15);

        // when
        challengeService.assignTeams(challenge.getId());

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        List<ChallengeParticipant> participants = challengeParticipantRepository
                .findAllByChallengeId(challenge.getId());

        assertSoftly(softly -> {
            softly.assertThat(teams).hasSize(1);
            softly.assertThat(participants).allMatch(p -> p.getChallengeTeamId() != null);
            softly.assertThat(participants).extracting("challengeTeamId").containsOnly(teams.get(0).getId());
        });
    }

    @Test
    void 참여자가_16명일_때_최대_15명_제한으로_2개의_팀이_생성된다() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 16);

        // when
        challengeService.assignTeams(challenge.getId());

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        assertThat(teams).hasSize(2); // ceil(16/15) = 2 (8 members per team)
    }

    @Test
    void 참여자가_22명일_때_최대_15명_제한으로_2개의_팀이_생성된다() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 22);

        // when
        challengeService.assignTeams(challenge.getId());

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        assertThat(teams).hasSize(2); // ceil(22/15) = 2 (11 members per team)
    }

    @Test
    void 참여자가_30명일_때_2개의_팀이_생성된다() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 30);

        // when
        challengeService.assignTeams(challenge.getId());

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        List<ChallengeParticipant> participants = challengeParticipantRepository
                .findAllByChallengeId(challenge.getId());

        assertSoftly(softly -> {
            softly.assertThat(teams).hasSize(2); // ceil(30/15) = 2
            softly.assertThat(participants).allMatch(p -> p.getChallengeTeamId() != null);
        });
    }

    @Test
    void 참여자가_31명일_때_최대_15명_제한으로_3개의_팀이_생성된다() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 31);

        // when
        challengeService.assignTeams(challenge.getId());

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        assertThat(teams).hasSize(3); // ceil(31/15) = 3 (10.33 members per team)
    }

    @Test
    void 참여자가_45명일_때_3개의_팀이_생성된다() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 45);

        // when
        challengeService.assignTeams(challenge.getId());

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        assertThat(teams).hasSize(3); // ceil(45/15) = 3
    }

    @Test
    void 참여자가_46명일_때_최대_15명_제한으로_4개의_팀이_생성된다() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 46);

        // when
        challengeService.assignTeams(challenge.getId());

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        assertThat(teams).hasSize(4); // ceil(46/15) = 4
    }

    @Test
    void 참여자가_5명일_때_최소_인원_1개의_팀이_생성된다() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 5);

        // when
        challengeService.assignTeams(challenge.getId());

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        assertThat(teams).hasSize(1);
    }

    private Challenge createChallenge() {
        return challengeRepository.save(Challenge.builder()
                .name("테스트 챌린지")
                .generation(1)
                .startDate(java.time.LocalDate.now())
                .endDate(java.time.LocalDate.now().plusDays(30))
                .totalDays(30)
                .build());
    }

    private void createParticipants(Long challengeId, int count) {
        List<ChallengeParticipant> participants = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            participants.add(ChallengeParticipant.builder()
                    .challengeId(challengeId)
                    .memberId((long) i)
                    .build());
        }
        challengeParticipantRepository.saveAll(participants);
    }
}
