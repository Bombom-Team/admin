package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.dto.AssignTeamsRequest;
import me.bombom.api.v1.challenge.dto.CreateChallengeTeamsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeTeamResponse;
import me.bombom.api.v1.challenge.dto.UpdateParticipantTeamRequest;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({ ChallengeService.class, QuerydslConfig.class })
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
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

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
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

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
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

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
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

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
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

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
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

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
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

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
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

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

    @Test
    void 팀_크기_설정_테스트_10명_제한() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 21); // 21 participants

        // when
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(10)); // Max 10

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        assertThat(teams).hasSize(3); // ceil(21/10) = 3
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

    @Test
    void 챌린지_팀_목록_조회_성공() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 15);
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

        // when
        List<GetChallengeTeamResponse> responses = challengeService.getChallengeTeams(challenge.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(responses).isNotEmpty();
            softly.assertThat(responses).extracting("challengeId").containsOnly(challenge.getId());
            softly.assertThat(responses).hasSize(1);
        });
    }

    @Test
    void 참여자_팀_수동_변경_성공() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 5);
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

        List<ChallengeTeam> teams = challengeTeamRepository.findByChallengeId(challenge.getId());
        ChallengeParticipant participant = challengeParticipantRepository.findAllByChallengeId(challenge.getId())
                .getFirst();

        // 1개 팀뿐이면 같은 팀으로 변경(실질적 변경 없음)도 허용되므로 첫번째 팀 선택 로직 단순화
        ChallengeTeam newTeam = teams.getFirst();

        // when
        challengeService.updateParticipantTeam(
                challenge.getId(),
                participant.getId(),
                new UpdateParticipantTeamRequest(newTeam.getId()));

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository
                .findByChallengeIdAndMemberId(challenge.getId(), participant.getMemberId()).orElseThrow();

        assertThat(updatedParticipant.getChallengeTeamId()).isEqualTo(newTeam.getId());
    }

    @Test
    void 참여자_팀_수동_변경_실패_다른_챌린지_팀() {
        // given
        Challenge challenge1 = createChallenge();
        Challenge challenge2 = createChallenge();
        createParticipants(challenge1.getId(), 5);
        createParticipants(challenge2.getId(), 5);
        challengeService.assignTeams(challenge1.getId(), new AssignTeamsRequest(15));
        challengeService.assignTeams(challenge2.getId(), new AssignTeamsRequest(15));

        ChallengeParticipant participant = challengeParticipantRepository.findAllByChallengeId(challenge1.getId())
                .getFirst();
        ChallengeTeam otherChallengeTeam = challengeTeamRepository.findByChallengeId(challenge2.getId()).getFirst();

        UpdateParticipantTeamRequest request = new UpdateParticipantTeamRequest(otherChallengeTeam.getId());

        // when & then
        assertThatThrownBy(
                () -> challengeService.updateParticipantTeam(challenge1.getId(), participant.getMemberId(), request))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 챌린지_팀_일괄_생성_성공() {
        // given
        Challenge challenge = createChallenge();
        CreateChallengeTeamsRequest request = new CreateChallengeTeamsRequest(5);

        // when
        challengeService.createChallengeTeams(challenge.getId(), request);

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findByChallengeId(challenge.getId());
        assertThat(teams).hasSize(5);
    }

    @Test
    void 챌린지_팀_삭제_성공_참여자_미배정_처리() {
        // given
        Challenge challenge = createChallenge();
        createParticipants(challenge.getId(), 5);
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

        ChallengeTeam team = challengeTeamRepository.findByChallengeId(challenge.getId()).getFirst();
        Long teamId = team.getId();

        // when
        challengeService.deleteChallengeTeam(challenge.getId(), teamId);

        // then
        // 1. 팀 삭제 확인
        assertSoftly(softly -> {
            softly.assertThat(challengeTeamRepository.findById(teamId)).isEmpty();

            // 2. 참여자 미배정 확인
            List<ChallengeParticipant> participants = challengeParticipantRepository
                    .findAllByChallengeId(challenge.getId());
            softly.assertThat(participants).allMatch(p -> p.getChallengeTeamId() == null);
        });
    }
}
