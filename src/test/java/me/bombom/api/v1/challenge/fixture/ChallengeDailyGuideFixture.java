package me.bombom.api.v1.challenge.fixture;

import static org.instancio.Select.field;

import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.DailyGuideType;
import org.instancio.Instancio;

public class ChallengeDailyGuideFixture {

    public static ChallengeDailyGuide createGuide(Long challengeId) {
        return createGuide(challengeId, 1, DailyGuideType.READ);
    }

    public static ChallengeDailyGuide createGuide(Long challengeId, int dayIndex, DailyGuideType type) {
        return Instancio.of(ChallengeDailyGuide.class)
                .set(field(ChallengeDailyGuide::getId), null)
                .set(field(ChallengeDailyGuide::getChallengeId), challengeId)
                .set(field(ChallengeDailyGuide::getDayIndex), dayIndex)
                .set(field(ChallengeDailyGuide::getType), type)
                .set(field(ChallengeDailyGuide::getImageUrl), "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/test.jpg")
                .set(field(ChallengeDailyGuide::getNotice), "테스트 안내")
                .set(field(ChallengeDailyGuide::isCommentEnabled), type == DailyGuideType.COMMENT)
                .create();
    }
}
