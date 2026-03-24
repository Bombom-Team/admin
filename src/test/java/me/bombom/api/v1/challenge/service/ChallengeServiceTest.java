package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.dto.AssignTeamsRequest;
import me.bombom.api.v1.challenge.dto.CreateChallengeTeamsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.UpdateParticipantTeamRequest;
import me.bombom.api.v1.challenge.dto.request.GrantShieldRequest;
import me.bombom.api.v1.challenge.fixture.ChallengeFixture;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.config.TimeConfig;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.fixture.MemberFixture;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import({ ChallengeService.class, QuerydslConfig.class, TimeConfig.class })
class ChallengeServiceTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private Clock clock;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeTeamRepository challengeTeamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 참여자가_15명일_때_1개의_팀이_생성된다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
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
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
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
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
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
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
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
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
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
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
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
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
        createParticipants(challenge.getId(), 46);

        // when
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        assertThat(teams).hasSize(4); // ceil(46/15) = 4 (11.5 members per team)
    }

    @Test
    void 참여자가_최대_배수일_때_팀마다_배정된_인원수가_동일하다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
        createParticipants(challenge.getId(), 45);

        // when
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        List<ChallengeParticipant> participants = challengeParticipantRepository
                .findAllByChallengeId(challenge.getId());

        assertSoftly(softly -> {
            for (ChallengeTeam team : teams) {
                long teamCount = participants.stream()
                        .filter(p -> team.getId().equals(p.getChallengeTeamId()))
                        .count();
                softly.assertThat(teamCount).isEqualTo(15);
            }
        });
    }

    @Test
    void 참여자가_최대_배수가_아닐_때_배정된_인원수의_차이가_최대_1명이다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
        createParticipants(challenge.getId(), 32);

        // when
        challengeService.assignTeams(challenge.getId(), new AssignTeamsRequest(15));

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        List<ChallengeParticipant> participants = challengeParticipantRepository
                .findAllByChallengeId(challenge.getId());

        assertSoftly(softly -> {
            long minCount = Long.MAX_VALUE;
            long maxCount = Long.MIN_VALUE;

            for (ChallengeTeam team : teams) {
                long teamCount = participants.stream()
                        .filter(p -> team.getId().equals(p.getChallengeTeamId()))
                        .count();
                minCount = Math.min(minCount, teamCount);
                maxCount = Math.max(maxCount, teamCount);
            }

            softly.assertThat(maxCount - minCount).isLessThanOrEqualTo(1);
        });
    }

    @Test
    void 챌린지_팀을_생성한다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
        CreateChallengeTeamsRequest request = new CreateChallengeTeamsRequest(3);

        // when
        challengeService.createChallengeTeams(challenge.getId(), request);

        // then
        List<ChallengeTeam> teams = challengeTeamRepository.findAll();
        assertThat(teams).hasSize(3);
    }

    @Test
    void 챌린지_팀을_삭제한다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
        ChallengeTeam team = challengeTeamRepository.save(ChallengeTeam.builder()
                .challengeId(challenge.getId())
                .progress(0)
                .build());

        // when
        challengeService.deleteChallengeTeam(challenge.getId(), team.getId());

        // then
        assertThat(challengeTeamRepository.existsById(team.getId())).isFalse();
    }

    @Test
    void 챌린지_팀_삭제_시_해당_팀원들의_팀ID는_null이_된다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
        ChallengeTeam team = challengeTeamRepository.save(ChallengeFixture.createTeam(challenge.getId()));

        Member member = memberRepository.save(MemberFixture.createMember("user1"));
        ChallengeParticipant participant = challengeParticipantRepository
                .save(ChallengeFixture.createParticipant(challenge.getId(), member.getId()));
        participant.assignTeam(team.getId());
        challengeParticipantRepository.save(participant);

        // when
        challengeService.deleteChallengeTeam(challenge.getId(), team.getId());

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId()).get();
        assertThat(updatedParticipant.getChallengeTeamId()).isNull();
    }

    @Test
    void 참여자의_팀을_수정한다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());
        ChallengeTeam team = challengeTeamRepository.save(ChallengeFixture.createTeam(challenge.getId()));

        Member member = memberRepository.save(MemberFixture.createMember("user1"));
        ChallengeParticipant participant = challengeParticipantRepository
                .save(ChallengeFixture.createParticipant(challenge.getId(), member.getId()));

        // when
        challengeService.updateParticipantTeam(challenge.getId(), participant.getId(),
                new UpdateParticipantTeamRequest(team.getId()));

        // then
        ChallengeParticipant updatedParticipant = challengeParticipantRepository.findById(participant.getId()).get();
        assertThat(updatedParticipant.getChallengeTeamId()).isEqualTo(team.getId());
    }

    @Test
    void 생존자들에게_지정한_개수만큼_쉴드를_지급한다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());

        Member m1 = memberRepository.save(MemberFixture.createMember("m1"));
        Member m2 = memberRepository.save(MemberFixture.createMember("m2"));
        Member m3 = memberRepository.save(MemberFixture.createMember("m3"));

        // 생존자 (m1, m2), 비생존자 (m3)
        challengeParticipantRepository.saveAll(List.of(
                ChallengeFixture.createParticipant(challenge.getId(), m1.getId(), true, 1),
                ChallengeFixture.createParticipant(challenge.getId(), m2.getId(), true, 0),
                ChallengeFixture.createParticipant(challenge.getId(), m3.getId(), false, 0)));

        GrantShieldRequest request = new GrantShieldRequest(2); // 2개 지급

        // when
        challengeService.grantShield(challenge.getId(), request);

        // then
        List<ChallengeParticipant> participants = challengeParticipantRepository
                .findAllByChallengeId(challenge.getId());

        assertSoftly(softly -> {
            softly.assertThat(
                    participants.stream().filter(p -> p.getMemberId().equals(m1.getId())).findFirst().get().getShield())
                    .isEqualTo(3);
            softly.assertThat(
                    participants.stream().filter(p -> p.getMemberId().equals(m2.getId())).findFirst().get().getShield())
                    .isEqualTo(2);

            // 비생존자: 0 (변동 없음)
            softly.assertThat(
                    participants.stream().filter(p -> p.getMemberId().equals(m3.getId())).findFirst().get().getShield())
                    .isEqualTo(0);
        });
    }

    @Test
    void 참여자_조회_시_쉴드_개수가_포함되며_생존_여부로_필터링할_수_있다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallenge());

        Member m1 = memberRepository.save(MemberFixture.createMember("survivor"));
        Member m2 = memberRepository.save(MemberFixture.createMember("failed"));

        challengeParticipantRepository.saveAll(List.of(
                ChallengeFixture.createParticipant(challenge.getId(), m1.getId(), true, 5),
                ChallengeFixture.createParticipant(challenge.getId(), m2.getId(), false, 2)));

        // when (생존자 필터링)
        GetChallengeParticipantsRequest request = new GetChallengeParticipantsRequest(null, null, true);
        Page<GetChallengeParticipantResponse> result = challengeService.getChallengeParticipants(challenge.getId(),
                request, PageRequest.of(0, 10));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getContent()).hasSize(1);
            softly.assertThat(result.getContent().get(0).nickname()).isEqualTo("survivor");
            softly.assertThat(result.getContent().get(0).shield()).isEqualTo(5);
            softly.assertThat(result.getContent().get(0).isSurvived()).isTrue();
        });

        // when (탈락자 필터링)
        GetChallengeParticipantsRequest failRequest = new GetChallengeParticipantsRequest(null, null, false);
        Page<GetChallengeParticipantResponse> failResult = challengeService.getChallengeParticipants(challenge.getId(),
                failRequest, PageRequest.of(0, 10));

        // then
        assertSoftly(softly -> {
            softly.assertThat(failResult.getContent()).hasSize(1);
            softly.assertThat(failResult.getContent().get(0).nickname()).isEqualTo("failed");
            softly.assertThat(failResult.getContent().get(0).shield()).isEqualTo(2);
            softly.assertThat(failResult.getContent().get(0).isSurvived()).isFalse();
        });
    }

    @Test
    void 오늘_시작하는_챌린지가_없으면_팀_배정이_발생하지_않는다() {
        // given
        challengeRepository.save(ChallengeFixture.createChallengeStartingOn(LocalDate.now(clock).plusDays(1)));

        // when
        challengeService.assignTeamsForTodayStartChallenges();

        // then
        assertThat(challengeTeamRepository.findAll()).isEmpty();
    }

    @Test
    void 오늘_시작하는_챌린지의_참여자들이_팀에_배정된다() {
        // given
        Challenge challenge = challengeRepository.save(ChallengeFixture.createChallengeStartingOn(LocalDate.now(clock)));
        createParticipants(challenge.getId(), 10);

        // when
        challengeService.assignTeamsForTodayStartChallenges();

        // then
        List<ChallengeParticipant> participants = challengeParticipantRepository.findAllByChallengeId(challenge.getId());
        assertThat(participants).allMatch(p -> p.getChallengeTeamId() != null);
    }

    @Test
    void 오늘_시작하는_챌린지가_여러_개면_모두_배정된다() {
        // given
        Challenge challenge1 = challengeRepository.save(ChallengeFixture.createChallengeStartingOn(LocalDate.now(clock)));
        Challenge challenge2 = challengeRepository.save(ChallengeFixture.createChallengeStartingOn(LocalDate.now(clock)));
        createParticipants(challenge1.getId(), 5, "c1user");
        createParticipants(challenge2.getId(), 5, "c2user");

        // when
        challengeService.assignTeamsForTodayStartChallenges();

        // then
        List<ChallengeParticipant> participants1 = challengeParticipantRepository.findAllByChallengeId(challenge1.getId());
        List<ChallengeParticipant> participants2 = challengeParticipantRepository.findAllByChallengeId(challenge2.getId());

        assertSoftly(softly -> {
            softly.assertThat(participants1).allMatch(p -> p.getChallengeTeamId() != null);
            softly.assertThat(participants2).allMatch(p -> p.getChallengeTeamId() != null);
        });
    }

    @Test
    void 오늘이_아닌_챌린지_참여자는_배정되지_않는다() {
        // given
        Challenge todayChallenge = challengeRepository.save(ChallengeFixture.createChallengeStartingOn(LocalDate.now(clock)));
        Challenge tomorrowChallenge = challengeRepository.save(ChallengeFixture.createChallengeStartingOn(LocalDate.now(clock).plusDays(1)));
        createParticipants(todayChallenge.getId(), 5, "today");
        createParticipants(tomorrowChallenge.getId(), 5, "tomorrow");

        // when
        challengeService.assignTeamsForTodayStartChallenges();

        // then
        List<ChallengeParticipant> tomorrowParticipants = challengeParticipantRepository.findAllByChallengeId(tomorrowChallenge.getId());
        assertThat(tomorrowParticipants).allMatch(p -> p.getChallengeTeamId() == null);
    }

    private void createParticipants(Long challengeId, int count) {
        createParticipants(challengeId, count, "user");
    }

    private void createParticipants(Long challengeId, int count, String nicknamePrefix) {
        List<ChallengeParticipant> participants = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Member member = memberRepository.save(MemberFixture.createMember(nicknamePrefix + i));
            participants.add(ChallengeFixture.createParticipant(challengeId, member.getId()));
        }
        challengeParticipantRepository.saveAll(participants);
    }
}
