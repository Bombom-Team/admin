package me.bombom.api.v1.challenge.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
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

    public Page<GetChallengeResponse> getChallenges(GetChallengesRequest request, Pageable pageable) {
        return challengeRepository.getChallenges(request, pageable);
    }

    public Page<GetChallengeParticipantResponse> getChallengeParticipants(
            Long challengeId,
            GetChallengeParticipantsRequest request,
            Pageable pageable) {
        return challengeParticipantRepository.getChallengeParticipants(challengeId, request, pageable);
    }
}
