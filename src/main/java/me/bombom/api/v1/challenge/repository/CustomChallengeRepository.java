package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomChallengeRepository {

    Page<GetChallengeResponse> getChallenges(GetChallengesRequest request, Pageable pageable);
}
