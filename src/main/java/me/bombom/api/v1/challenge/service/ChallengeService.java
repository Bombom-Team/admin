package me.bombom.api.v1.challenge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.dto.GetChallengeDetailResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeTeamRepository challengeTeamRepository;

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

    @Transactional
    public void assignTeams(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge"));
        List<ChallengeParticipant> participants = challengeParticipantRepository.findAllByChallengeId(challengeId);

        if (participants.isEmpty()) {
            return;
        }

        Collections.shuffle(participants);

        int teamCount = calculateTeamCount(participants.size());
        List<ChallengeTeam> teams = createTeams(challenge.getId(), teamCount);
        challengeTeamRepository.saveAll(teams);
        assignParticipantsToTeams(participants, teams);
    }

    private int calculateTeamCount(int totalParticipants) {
        int maxTeamSize = 15;
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
}
