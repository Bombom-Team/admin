package me.bombom.api.v1.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideRequest;
import me.bombom.api.v1.challenge.fixture.ChallengeDailyGuideFixture;
import me.bombom.api.v1.challenge.fixture.ChallengeFixture;
import me.bombom.api.v1.challenge.repository.ChallengeDailyGuideRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.file.service.S3FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import({ ChallengeDailyGuideService.class, QuerydslConfig.class })
class ChallengeDailyGuideServiceTest {

    @Autowired
    private ChallengeDailyGuideService dailyGuideService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeDailyGuideRepository dailyGuideRepository;

    @MockitoBean
    private S3FileService s3FileService;

    @Test
    void 새_이미지를_업로드해서_데일리_가이드를_생성한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes());
        CreateDailyGuideRequest request = new CreateDailyGuideRequest(1, DailyGuideType.READ, "day1-guide", null, "안내");
        String uploadedUrl = "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1-guide.jpg";

        given(s3FileService.uploadToChallengeBucket(any(), eq("day1-guide"))).willReturn(uploadedUrl);

        // when
        dailyGuideService.create(challengeId, image, request);

        // then
        List<ChallengeDailyGuide> guides = dailyGuideRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(guides).hasSize(1);
            softly.assertThat(guides.get(0).getImageUrl()).isEqualTo(uploadedUrl);
            softly.assertThat(guides.get(0).getDayIndex()).isEqualTo(1);
        });
    }

    @Test
    void 기존_이미지_URL로_데일리_가이드를_생성한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        String imageUrl = "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/existing.jpg";
        CreateDailyGuideRequest request = new CreateDailyGuideRequest(1, DailyGuideType.READ, null, imageUrl, "안내");

        // when
        dailyGuideService.create(challengeId, null, request);

        // then
        List<ChallengeDailyGuide> guides = dailyGuideRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(guides).hasSize(1);
            softly.assertThat(guides.get(0).getImageUrl()).isEqualTo(imageUrl);
        });
        then(s3FileService).should(never()).uploadToChallengeBucket(any(), any());
    }

    @Test
    void image와_imageUrl_모두_없으면_예외가_발생한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        CreateDailyGuideRequest request = new CreateDailyGuideRequest(1, DailyGuideType.READ, null, null, null);

        // when // then
        assertThatThrownBy(() -> dailyGuideService.create(challengeId, null, request))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 데일리_가이드_목록을_dayIndex_오름차순으로_조회한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId, 3, DailyGuideType.REMIND));
        dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId, 1, DailyGuideType.READ));
        dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId, 2, DailyGuideType.COMMENT));

        // when
        List<GetDailyGuideResponse> guides = dailyGuideService.getDailyGuides(challengeId);

        // then
        assertThat(guides).extracting(GetDailyGuideResponse::dayIndex).containsExactly(1, 2, 3);
    }

    @Test
    void 데일리_가이드_단건을_조회한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        Long guideId = dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId)).getId();

        // when
        GetDailyGuideResponse response = dailyGuideService.getDailyGuide(challengeId, guideId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.id()).isEqualTo(guideId);
            softly.assertThat(response.challengeId()).isEqualTo(challengeId);
        });
    }

    @Test
    void 다른_챌린지의_가이드는_조회할_수_없다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        Long otherChallengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        Long guideId = dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(otherChallengeId)).getId();

        // when // then
        assertThatThrownBy(() -> dailyGuideService.getDailyGuide(challengeId, guideId))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 새_이미지를_업로드해서_데일리_가이드를_수정한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        Long guideId = dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId)).getId();
        MockMultipartFile newImage = new MockMultipartFile("image", "new.jpg", "image/jpeg", "new".getBytes());
        UpdateDailyGuideRequest request = new UpdateDailyGuideRequest(2, DailyGuideType.COMMENT, "new-guide", null, "새 안내");
        String newUrl = "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/new-guide.jpg";

        given(s3FileService.uploadToChallengeBucket(any(), eq("new-guide"))).willReturn(newUrl);

        // when
        dailyGuideService.update(challengeId, guideId, newImage, request);

        // then
        ChallengeDailyGuide updated = dailyGuideRepository.findById(guideId).get();
        assertSoftly(softly -> {
            softly.assertThat(updated.getDayIndex()).isEqualTo(2);
            softly.assertThat(updated.getType()).isEqualTo(DailyGuideType.COMMENT);
            softly.assertThat(updated.getImageUrl()).isEqualTo(newUrl);
            softly.assertThat(updated.isCommentEnabled()).isTrue();
        });
    }

    @Test
    void 기존_이미지_URL로_데일리_가이드를_수정한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        Long guideId = dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId)).getId();
        String newImageUrl = "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/other.jpg";
        UpdateDailyGuideRequest request = new UpdateDailyGuideRequest(null, null, null, newImageUrl, null);

        // when
        dailyGuideService.update(challengeId, guideId, null, request);

        // then
        ChallengeDailyGuide updated = dailyGuideRepository.findById(guideId).get();
        assertThat(updated.getImageUrl()).isEqualTo(newImageUrl);
        then(s3FileService).should(never()).uploadToChallengeBucket(any(), any());
    }

    @Test
    void 이미지_없이_수정하면_기존_이미지가_유지된다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        ChallengeDailyGuide guide = dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId));
        String originalImageUrl = guide.getImageUrl();

        UpdateDailyGuideRequest request = new UpdateDailyGuideRequest(2, null, null, null, null);

        // when
        dailyGuideService.update(challengeId, guide.getId(), null, request);

        // then
        ChallengeDailyGuide updated = dailyGuideRepository.findById(guide.getId()).get();
        assertSoftly(softly -> {
            softly.assertThat(updated.getDayIndex()).isEqualTo(2);
            softly.assertThat(updated.getImageUrl()).isEqualTo(originalImageUrl);
        });
        then(s3FileService).should(never()).uploadToChallengeBucket(any(), any());
    }

    @Test
    void 데일리_가이드를_삭제한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        Long guideId = dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId)).getId();

        // when
        dailyGuideService.delete(challengeId, guideId);

        // then
        assertThat(dailyGuideRepository.existsById(guideId)).isFalse();
    }

    @Test
    void 존재하지_않는_챌린지에_접근하면_예외가_발생한다() {
        // given
        Long notExistChallengeId = 999L;

        // when // then
        assertThatThrownBy(() -> dailyGuideService.getDailyGuides(notExistChallengeId))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_가이드에_접근하면_예외가_발생한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        Long notExistGuideId = 999L;

        // when // then
        assertThatThrownBy(() -> dailyGuideService.getDailyGuide(challengeId, notExistGuideId))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 새_이미지_업로드로_생성_시_같은_dayIndex가_존재하면_예외가_발생한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId, 1, DailyGuideType.READ));

        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes());
        CreateDailyGuideRequest request = new CreateDailyGuideRequest(1, DailyGuideType.REMIND, "day1-guide", null, null);

        // when // then
        assertThatThrownBy(() -> dailyGuideService.create(challengeId, image, request))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 기존_이미지로_생성_시_같은_dayIndex가_존재하면_예외가_발생한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId, 1, DailyGuideType.READ));

        CreateDailyGuideRequest request = new CreateDailyGuideRequest(
                1, DailyGuideType.REMIND, null,
                "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/other.jpg", null);

        // when // then
        assertThatThrownBy(() -> dailyGuideService.create(challengeId, null, request))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 수정_시_같은_dayIndex가_존재하면_예외가_발생한다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId, 2, DailyGuideType.READ));
        Long guideId = dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId, 1, DailyGuideType.READ)).getId();

        UpdateDailyGuideRequest request = new UpdateDailyGuideRequest(2, null, null, null, null);

        // when // then
        assertThatThrownBy(() -> dailyGuideService.update(challengeId, guideId, null, request))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 수정_시_자신의_dayIndex로_수정하면_예외가_발생하지_않는다() {
        // given
        Long challengeId = challengeRepository.save(ChallengeFixture.createChallenge()).getId();
        Long guideId = dailyGuideRepository.save(ChallengeDailyGuideFixture.createGuide(challengeId, 1, DailyGuideType.READ)).getId();

        UpdateDailyGuideRequest request = new UpdateDailyGuideRequest(1, DailyGuideType.REMIND, null, null, null);

        // when // then (no exception)
        dailyGuideService.update(challengeId, guideId, null, request);
    }
}
