package me.bombom.api.v1.challenge.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideRequest;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeDailyGuideService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeDailyGuideRepository dailyGuideRepository;

    @Transactional
    public void create(Long challengeId, CreateDailyGuideRequest request) {
        validateChallengeExists(challengeId);
        dailyGuideRepository.save(request.toEntity(challengeId));
    }

    public List<GetDailyGuideResponse> getDailyGuides(Long challengeId) {
        validateChallengeExists(challengeId);
        return dailyGuideRepository.findByChallengeIdOrderByDayIndexAsc(challengeId)
                .stream()
                .map(GetDailyGuideResponse::from)
                .toList();
    }

    public GetDailyGuideResponse getDailyGuide(Long challengeId, Long guideId) {
        ChallengeDailyGuide guide = findGuide(challengeId, guideId);
        return GetDailyGuideResponse.from(guide);
    }

    @Transactional
    public void update(Long challengeId, Long guideId, UpdateDailyGuideRequest request) {
        ChallengeDailyGuide guide = findGuide(challengeId, guideId);
        guide.update(request.dayIndex(), request.type(), request.imageUrl(), request.notice(), request.commentEnabled());
    }

    @Transactional
    public void delete(Long challengeId, Long guideId) {
        ChallengeDailyGuide guide = findGuide(challengeId, guideId);
        dailyGuideRepository.delete(guide);
    }

    private void validateChallengeExists(Long challengeId) {
        if (!challengeRepository.existsById(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("entity", "Challenge")
                    .addContext("challengeId", challengeId);
        }
    }

    private ChallengeDailyGuide findGuide(Long challengeId, Long guideId) {
        validateChallengeExists(challengeId);
        ChallengeDailyGuide guide = dailyGuideRepository.findById(guideId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("entity", "ChallengeDailyGuide")
                        .addContext("guideId", guideId));
        if (!guide.getChallengeId().equals(challengeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("entity", "ChallengeDailyGuide")
                    .addContext("guideId", guideId)
                    .addContext("challengeId", challengeId)
                    .addContext("reason", "해당 챌린지에 속하지 않는 가이드입니다.");
        }
        return guide;
    }
}
