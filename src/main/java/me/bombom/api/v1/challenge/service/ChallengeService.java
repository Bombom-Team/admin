package me.bombom.api.v1.challenge.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.dto.AssignTeamsRequest;
import me.bombom.api.v1.challenge.dto.CreateChallengeTeamsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeDayResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeDetailResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeTeamResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateParticipantTeamRequest;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeRequest;
import me.bombom.api.v1.challenge.dto.request.GrantShieldRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeRequest;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private static final List<ChallengeTodoType> DEFAULT_CHALLENGE_TODO_TYPES = List.of(
            ChallengeTodoType.READ,
            ChallengeTodoType.COMMENT,
            ChallengeTodoType.MINDSET
    );

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeTeamRepository challengeTeamRepository;
    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeDailyGuideRepository dailyGuideRepository;
    private final NewsletterGroupRepository newsletterGroupRepository;
    private final Clock clock;

    public Page<GetChallengeResponse> getChallenges(GetChallengesRequest request, Pageable pageable) {
        return challengeRepository.getChallenges(request, pageable);
    }

    public GetChallengeDetailResponse getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .map(GetChallengeDetailResponse::from)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge"));
    }

    public Page<GetChallengeParticipantResponse> getChallengeParticipants(
            Long challengeId,
            GetChallengeParticipantsRequest request,
            Pageable pageable) {
        return challengeParticipantRepository.getChallengeParticipants(challengeId, request, pageable);
    }

    public List<GetChallengeDayResponse> getChallengeSchedule(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge"));

        Map<Integer, GetDailyGuideResponse> guideByDayIndex = dailyGuideRepository.findAllByChallengeIdAsResponse(challengeId)
                .stream()
                .collect(Collectors.toMap(GetDailyGuideResponse::dayIndex, g -> g));

        List<GetChallengeDayResponse> schedule = new ArrayList<>();
        LocalDate date = challenge.getStartDate();
        while (!date.isAfter(challenge.getEndDate())) {
            int dayIndex = calculateDayIndex(challenge.getStartDate(), date);
            GetDailyGuideResponse guide = guideByDayIndex.get(dayIndex);
            schedule.add(new GetChallengeDayResponse(
                    date, date.getDayOfWeek(), dayIndex,
                    guide != null ? guide.type() : null,
                    guide != null ? guide.imageUrl() : null));
            date = date.plusDays(1);
        }
        return schedule;
    }

    public List<GetChallengeTeamResponse> getChallengeTeams(Long challengeId) {
        return challengeTeamRepository.findByChallengeId(challengeId).stream()
                .map(GetChallengeTeamResponse::from)
                .toList();
    }

    @Transactional
    public void assignTeamsForTodayStartChallenges() {
        LocalDate today = LocalDate.now(clock);
        List<Challenge> challenges = challengeRepository.findAllByStartDate(today);

        if (challenges.isEmpty()) {
            return;
        }

        log.info("[ChallengeTeamAssignment] 당일 시작 챌린지 {}개 팀 자동 배정 시작", challenges.size());

        for (Challenge challenge : challenges) {
            try {
                assignTeams(challenge.getId(), new AssignTeamsRequest(null));
                log.info("[ChallengeTeamAssignment] 챌린지 id={} 팀 배정 완료", challenge.getId());
            } catch (Exception e) {
                log.error("[ChallengeTeamAssignment] 챌린지 id={} 팀 배정 실패", challenge.getId(), e);
            }
        }
    }

    @Transactional
    public void assignTeams(Long challengeId, AssignTeamsRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge"));
        List<ChallengeParticipant> participants = challengeParticipantRepository.findAllByChallengeId(challengeId);

        if (participants.isEmpty()) {
            return;
        }

        Collections.shuffle(participants);

        int teamCount = calculateTeamCount(participants.size(), request.maxTeamSize());
        List<ChallengeTeam> existingTeams = challengeTeamRepository.findByChallengeId(challengeId);

        if (existingTeams.size() < teamCount) {
            int missingTeamCount = teamCount - existingTeams.size();
            List<ChallengeTeam> newTeams = challengeTeamRepository
                    .saveAll(createTeams(challenge.getId(), missingTeamCount));
            existingTeams.addAll(newTeams);
        }
        assignParticipantsToTeams(participants, existingTeams);
    }

    @Transactional
    public void updateParticipantTeam(Long challengeId, Long participantId, UpdateParticipantTeamRequest request) {
        if (!challengeRepository.existsById(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge");
        }

        ChallengeParticipant participant = challengeParticipantRepository.findById(participantId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant"));

        ChallengeTeam team = challengeTeamRepository.findById(request.challengeTeamId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeTeam"));

        if (!team.getChallengeId().equals(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.REASON, "해당 Challege에 없는 teamId입니다.");
        }

        participant.assignTeam(team.getId());
    }

    @Transactional
    public void createChallengeTeams(Long challengeId, CreateChallengeTeamsRequest request) {
        if (!challengeRepository.existsById(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge");
        }

        List<ChallengeTeam> teams = createTeams(challengeId, request.count());
        challengeTeamRepository.saveAll(teams);
    }

    @Transactional
    public void deleteChallengeTeam(Long challengeId, Long teamId) {
        ChallengeTeam team = challengeTeamRepository.findById(teamId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeTeam"));

        if (!team.getChallengeId().equals(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeTeam");
        }

        challengeParticipantRepository.updateChallengeTeamIdToNull(teamId);
        challengeTeamRepository.delete(team);
    }

    private int calculateDayIndex(LocalDate startDate, LocalDate date) {
        if (isWeekend(date)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(startDate, date) + 1;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private int calculateTeamCount(int totalParticipants, int maxTeamSize) {
        return Math.max(1, (int) Math.ceil((double) totalParticipants / maxTeamSize));
    }

    private List<ChallengeTeam> createTeams(Long challengeId, int teamCount) {
        List<ChallengeTeam> teams = new ArrayList<>();
        for (int i = 0; i < teamCount; i++) {
            teams.add(ChallengeTeam.builder()
                    .challengeId(challengeId)
                    .progress(0)
                    .build());
        }
        return teams;
    }

    private void assignParticipantsToTeams(List<ChallengeParticipant> participants, List<ChallengeTeam> teams) {
        int teamCount = teams.size();
        for (int i = 0; i < participants.size(); i++) {
            ChallengeParticipant participant = participants.get(i);
            ChallengeTeam team = teams.get(i % teamCount);
            participant.assignTeam(team.getId());
        }
    }

    @Transactional
    public void createChallenge(CreateChallengeRequest request) {
        validateNewsletterGroupExists(request.newsletterGroupId());
        Challenge challenge = Challenge.builder()
                .name(request.name())
                .generation(request.generation())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .newsletterGroupId(request.newsletterGroupId())
                .build();
        Challenge savedChallenge = challengeRepository.save(challenge);
        challengeTodoRepository.saveAll(createDefaultChallengeTodos(savedChallenge.getId()));
    }

    @Transactional
    public void updateChallenge(Long challengeId, UpdateChallengeRequest request) {
        if (request.newsletterGroupId() != null) {
            validateNewsletterGroupExists(request.newsletterGroupId());
        }
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "updateChallenge")
                        .addContext("challengeId", challengeId));
        challenge.update(request.name(), request.generation(), request.startDate(), request.endDate(), request.newsletterGroupId());
    }

    private void validateNewsletterGroupExists(Long newsletterGroupId) {
        if (!newsletterGroupRepository.existsById(newsletterGroupId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterGroup")
                    .addContext("newsletterGroupId", newsletterGroupId);
        }
    }

    private List<ChallengeTodo> createDefaultChallengeTodos(Long challengeId) {
        return DEFAULT_CHALLENGE_TODO_TYPES.stream()
                .map(todoType -> ChallengeTodo.builder()
                        .challengeId(challengeId)
                        .todoType(todoType)
                        .build())
                .toList();
    }

    @Transactional
    public void deleteChallenge(Long challengeId) {
        if (!challengeRepository.existsById(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                    .addContext(ErrorContextKeys.OPERATION, "deleteChallenge")
                    .addContext("challengeId", challengeId);
        }
        if (challengeParticipantRepository.existsByChallengeId(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.CHALLENGE_HAS_PARTICIPANTS)
                    .addContext(ErrorContextKeys.OPERATION, "deleteChallenge")
                    .addContext("challengeId", challengeId);
        }
        challengeRepository.deleteById(challengeId);
    }

    @Transactional
    public void grantShield(Long challengeId, GrantShieldRequest request) {
        if (!challengeRepository.existsById(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge");
        }

        challengeParticipantRepository.incrementShieldByChallengeId(challengeId, request.count());
    }
}
