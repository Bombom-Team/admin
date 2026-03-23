package me.bombom.api.v1.challenge.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideFromImageRequest;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideFromImageRequest;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideRequest;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.file.service.S3FileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeDailyGuideService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeDailyGuideRepository dailyGuideRepository;
    private final S3FileService s3FileService;

    public List<String> getChallengeImages() {
        return s3FileService.listChallengeImages();
    }

    @Transactional
    public void create(Long challengeId, MultipartFile image, CreateDailyGuideRequest request) {
        validateChallengeExists(challengeId);
        validateDayIndexNotDuplicated(challengeId, request.dayIndex());
        String imageUrl = s3FileService.uploadToChallengeBucket(image, request.fileName());
        dailyGuideRepository.save(request.toEntity(challengeId, imageUrl));
    }

    @Transactional
    public void createFromImage(Long challengeId, CreateDailyGuideFromImageRequest request) {
        validateChallengeExists(challengeId);
        validateDayIndexNotDuplicated(challengeId, request.dayIndex());
        dailyGuideRepository.save(request.toEntity(challengeId));
    }

    public List<GetDailyGuideResponse> getDailyGuides(Long challengeId) {
        validateChallengeExists(challengeId);
        return dailyGuideRepository.findAllByChallengeIdAsResponse(challengeId);
    }

    public GetDailyGuideResponse getDailyGuide(Long challengeId, Long guideId) {
        validateChallengeExists(challengeId);
        return dailyGuideRepository.findByIdAndChallengeIdAsResponse(guideId, challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("entity", "ChallengeDailyGuide")
                        .addContext("guideId", guideId)
                        .addContext("challengeId", challengeId));
    }

    public GetDailyGuideResponse getDailyGuideByDayIndex(Long challengeId, int dayIndex) {
        validateChallengeExists(challengeId);
        return dailyGuideRepository.findByChallengeIdAndDayIndexAsResponse(challengeId, dayIndex)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("entity", "ChallengeDailyGuide")
                        .addContext("challengeId", challengeId)
                        .addContext("dayIndex", dayIndex));
    }

    @Transactional
    public void update(Long challengeId, Long guideId, MultipartFile image, UpdateDailyGuideRequest request) {
        ChallengeDailyGuide guide = findGuide(challengeId, guideId);
        if (request.dayIndex() != null) {
            validateDayIndexNotDuplicatedExcluding(challengeId, request.dayIndex(), guideId);
        }
        String imageUrl = image != null ? s3FileService.uploadToChallengeBucket(image, request.fileName()) : null;
        Boolean commentEnabled = request.type() != null ? request.type() == DailyGuideType.COMMENT : null;
        guide.update(request.dayIndex(), request.type(), imageUrl, request.notice(), commentEnabled);
    }

    @Transactional
    public void updateFromImage(Long challengeId, Long guideId, UpdateDailyGuideFromImageRequest request) {
        ChallengeDailyGuide guide = findGuide(challengeId, guideId);
        if (request.dayIndex() != null) {
            validateDayIndexNotDuplicatedExcluding(challengeId, request.dayIndex(), guideId);
        }
        Boolean commentEnabled = request.type() != null ? request.type() == DailyGuideType.COMMENT : null;
        guide.update(request.dayIndex(), request.type(), request.imageUrl(), request.notice(), commentEnabled);
    }

    @Transactional
    public void delete(Long challengeId, Long guideId) {
        ChallengeDailyGuide guide = findGuide(challengeId, guideId);
        dailyGuideRepository.delete(guide);
    }

    private void validateDayIndexNotDuplicated(Long challengeId, int dayIndex) {
        if (dailyGuideRepository.existsByChallengeIdAndDayIndex(challengeId, dayIndex)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext("challengeId", challengeId)
                    .addContext("dayIndex", dayIndex);
        }
    }

    private void validateDayIndexNotDuplicatedExcluding(Long challengeId, int dayIndex, Long excludeGuideId) {
        if (dailyGuideRepository.existsByChallengeIdAndDayIndexAndIdNot(challengeId, dayIndex, excludeGuideId)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext("challengeId", challengeId)
                    .addContext("dayIndex", dayIndex);
        }
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
