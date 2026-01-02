package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomChallengeParticipantRepository {

    Page<GetChallengeParticipantResponse> getChallengeParticipants(
            Long challengeId,
            GetChallengeParticipantsRequest request,
            Pageable pageable
    );
}
